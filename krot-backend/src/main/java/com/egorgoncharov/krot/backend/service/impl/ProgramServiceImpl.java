package com.egorgoncharov.krot.backend.service.impl;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.RangeFilter;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.common.ReactiveRepository;
import com.egorgoncharov.krot.backend.model.entity.ProgramEntity;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.repository.ProgramRepository;
import com.egorgoncharov.krot.backend.model.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.service.ProgramService;
import com.egorgoncharov.krot.backend.service.common.ReactiveCrudService;
import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;
import io.quarkus.panache.common.Parameters;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProgramServiceImpl extends ReactiveCrudService<ProgramEntity, UUID> implements ProgramService {
    private final SecurityIdentity client;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;

    @Inject
    public ProgramServiceImpl(SecurityIdentity client, ProgramRepository programRepository, UserRepository userRepository) {
        this.client = client;
        this.programRepository = programRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected ReactiveRepository<ProgramEntity, UUID> repository() {
        return programRepository;
    }

    @Override
    public Uni<Result<Page<ProgramEntity>>> filter(List<UUID> ids, TimeRangeFilter creationTime, UUID ownerId, String nameQuery, PaginationOptions pagination) {
        StringBuilder query = new StringBuilder("1=1");
        Parameters parameters = new Parameters();
        if (ids != null && !ids.isEmpty()) {
            query.append(" AND id IN :ids");
            parameters.and("ids", ids);
        }
        RangeFilter.applyRangeFilter(query, "creationTime", parameters, creationTime);
        if (ownerId != null) {
            query.append(" AND ownerId = :ownerId");
            parameters.and("ownerId", ownerId);
        }
        if (nameQuery != null) {
            query.append(" AND (lower(name)) LIKE :name");
            parameters.and("name", "%" + nameQuery.toLowerCase() + "%");
        }
        return executeFilter(query.toString(), parameters, pagination);
    }

    @Override
    public Uni<Result<ProgramEntity>> create(ProgramEntity o) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfEdit = clientUser.getRole().getAuthorities().contains(Authority.SELF_UPSERT);
        boolean canEditAnyProgram = clientUser.getRole().getAuthorities().contains(Authority.X_PROGRAM_UPSERT);
        boolean canEditProgram = clientUser.getRole().getAuthorities().contains(Authority.PROGRAM_UPSERT);
        if (!canEditAnyProgram && !canEditProgram && !canSelfEdit) return Uni.createFrom().item(Result.forbidden());
        o.setOwner(UserEntity.builder().id(clientUser.getId()).build());
        o.setCreatedAt(OffsetDateTime.now());
        return programRepository.existsByName(o.getName()).chain(exists -> exists ? Uni.createFrom().item(Result.conflict("name")) : super.create(o));
    }

    @Override
    public Uni<Result<ProgramEntity>> update(ProgramEntity o) {
        return modifyProgram(o.getId(), false).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.nullCast());
            ProgramEntity program = result.getResult().get();
            Uni<Boolean> nameExistsUni = (o.getName() == null || o.getName().equals(program.getName())) ? Uni.createFrom().item(false) : programRepository.find("name = ?1 AND id != ?2", o.getName(), o.getId()).count().map(c -> c > 0);
            return nameExistsUni.chain(exists -> {
                if (exists) return Uni.createFrom().item(Result.conflict("name"));
                o.setOwner(null);
                o.setCreatedAt(null);
                return super.update(o);
            });
        });
    }

    @Override
    public Uni<Result<Void>> transferOwnership(UUID programId, UUID newOwnerId) {
        return modifyProgram(programId, true).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.nullCast());
            ProgramEntity program = result.getResult().get();
            return userRepository.existsById(newOwnerId).chain(exists -> {
                if (!exists) return Uni.createFrom().item(Result.notFound().voidCast());
                program.setOwner(UserEntity.builder().id(newOwnerId).build());
                program.setName(null);
                program.setCreatedAt(null);
                return programRepository.save(program).replaceWith(Result.ok().voidCast());
            });
        });
    }

    @Override
    public Uni<Result<List<ProgramEntity>>> deleteById(List<UUID> ids) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfUpsert = clientUser.getRole().getAuthorities().contains(Authority.SELF_UPSERT);
        boolean canDeleteAnyProgram = clientUser.getRole().getAuthorities().contains(Authority.X_PROGRAM_DELETE);
        boolean canDeleteProgram = clientUser.getRole().getAuthorities().contains(Authority.PROGRAM_DELETE);
        if (!canSelfUpsert && !canDeleteAnyProgram && !canDeleteProgram) return Uni.createFrom().item(Result.forbidden());
        String query = canDeleteAnyProgram ? "FROM ProgramEntity p WHERE p.id IN :ids" : "FROM ProgramEntity p WHERE p.id IN :ids AND" + (canSelfUpsert ? " (p.owner.role.grade < :clientGrade OR p.owner.id = :clientId)" : " p.owner.role.grade < :clientGrade");
        Parameters parameters = Parameters.with("ids", ids);
        if (!canDeleteAnyProgram) {
            parameters.and("clientGrade", clientUser.getRole().getGrade());
            if (canSelfUpsert) parameters.and("clientId", clientUser.getId());
        }
        return programRepository.find(query, parameters.map()).list().chain(authorizedDevices -> {
            if (authorizedDevices.size() != ids.size()) return Uni.createFrom().item(Result.notFound());
            return super.deleteById(ids);
        });
    }

    private Uni<Result<ProgramEntity>> modifyProgram(UUID programId, boolean requireTransferAuthority) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfEdit = clientUser.getRole().getAuthorities().contains(Authority.SELF_UPSERT);
        boolean canEditAnyProgram = clientUser.getRole().getAuthorities().contains(Authority.X_PROGRAM_UPSERT);
        boolean canEditProgram = clientUser.getRole().getAuthorities().contains(Authority.PROGRAM_UPSERT);
        boolean canSelfTransferProgram = clientUser.getRole().getAuthorities().contains(Authority.SELF_PROGRAM_TRANSFER_OWNERSHIP);
        boolean canTransferAnyProgram = clientUser.getRole().getAuthorities().contains(Authority.X_PROGRAM_TRANSFER_OWNERSHIP);
        boolean canTransferProgram = clientUser.getRole().getAuthorities().contains(Authority.PROGRAM_TRANSFER_OWNERSHIP);
        return programRepository.findById(programId).chain(program -> {
            if (program == null) return Uni.createFrom().item(Result.notFound());
            boolean selfCase = program.getOwner().getId().equals(clientUser.getId()) && canSelfEdit;
            boolean gradeAllowed = clientUser.getRole().getGrade() > program.getOwner().getRole().getGrade() && canEditProgram;
            boolean canTransferOwnership = !requireTransferAuthority || ((selfCase && canSelfTransferProgram) || (gradeAllowed && canTransferProgram) || canTransferAnyProgram);
            if ((!selfCase && !gradeAllowed && !canEditAnyProgram) || !canTransferOwnership) return Uni.createFrom().item(Result.forbidden());
            return Uni.createFrom().item(Result.ok(program));
        });
    }
}
