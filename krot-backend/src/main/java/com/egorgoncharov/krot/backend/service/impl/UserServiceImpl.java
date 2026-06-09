package com.egorgoncharov.krot.backend.service.impl;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.RangeFilter;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.relational.RelationalReactiveRepository;
import com.egorgoncharov.krot.backend.model.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.relational.repository.HistoricalSessionRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.RoleRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.service.UserService;
import com.egorgoncharov.krot.backend.service.common.ReactiveCrudService;
import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UserServiceImpl extends ReactiveCrudService<UserEntity, UUID> implements UserService {
    private final SecurityIdentity client;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HistoricalSessionRepository historicalSessionRepository;

    @Inject
    public UserServiceImpl(SecurityIdentity client, UserRepository userRepository, RoleRepository roleRepository, HistoricalSessionRepository historicalSessionRepository) {
        this.client = client;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.historicalSessionRepository = historicalSessionRepository;
    }

    @Override
    protected RelationalReactiveRepository<UserEntity, UUID> repository() {
        return userRepository;
    }

    @WithSession
    @Override
    public Uni<Result<Page<UserEntity>>> filter(List<UUID> ids, Boolean active, TimeRangeFilter creationTime, UUID roleId, String usernameQuery, PaginationOptions pagination) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> parameters = new HashMap<>();
        if (!clientUser.getRole().getAuthorities().contains(Authority.X_USER_READ)) {
            if (clientUser.getRole().getAuthorities().contains(Authority.USER_READ)) {
                query.append(" AND role.grade <= :clientGrade");
                parameters.put("clientGrade", clientUser.getRole().getGrade());
            } else if (clientUser.getRole().getAuthorities().contains(Authority.SELF_READ)) {
                query.append(" AND id = :clientId");
                parameters.put("clientId", clientUser.getId());
                return executeFilter(query.toString(), parameters, pagination);
            } else {
                return Uni.createFrom().item(Result.forbidden());
            }
        }
        if (ids != null && !ids.isEmpty()) {
            query.append(" AND id IN :ids");
            parameters.put("ids", ids);
        }
        if (active != null) {
            query.append(" AND active = :active");
            parameters.put("active", active);
        }
        RangeFilter.applyRangeFilter(query, "createdAt", parameters, creationTime);
        if (roleId != null) {
            query.append(" AND role.id = :roleId");
            parameters.put("roleId", roleId.toString());
        }
        if (usernameQuery != null) {
            query.append(" AND (lower(username)) LIKE :username");
            parameters.put("username", "%" + usernameQuery.toLowerCase() + "%");
        }
        return executeFilter(query.toString(), parameters, pagination);
    }

    @WithTransaction
    @Override
    public Uni<Result<Void>> updatePassword(UserEntity user, String newPassword) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        if (!user.getId().equals(clientUser.getId())) return Uni.createFrom().item(Result.forbidden());
        if (!clientUser.getRole().getAuthorities().contains(Authority.SELF_UPSERT_PASSWORD)) return Uni.createFrom().item(Result.forbidden());
        if (!user.getPassword().equals(clientUser.getPassword())) return Uni.createFrom().item(Result.forbidden());
        return update(UserEntity.builder().id(user.getId()).password(newPassword).build()).map(Result::voidCast);
    }

    @WithTransaction
    @Override
    public Uni<Result<UserEntity>> create(UserEntity o) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canEditAnyRole = clientUser.getRole().getAuthorities().contains(Authority.X_ROLE_UPSERT);
        boolean canEditRole = clientUser.getRole().getAuthorities().contains(Authority.ROLE_UPSERT);
        o.setCreatedAt(OffsetDateTime.now());
        return roleRepository.findById(o.getRole().getId()).chain(role -> {
            if (role == null) return Uni.createFrom().item(Result.notFound());
            boolean gradeAllowed = clientUser.getRole().getGrade() > role.getGrade() && canEditRole;
            if (!gradeAllowed && !canEditAnyRole) return Uni.createFrom().item(Result.forbidden());
            o.setActive(true);
            o.setCreatedAt(OffsetDateTime.now());
            return userRepository.existsByName(o.getUsername()).chain(exists -> exists ? Uni.createFrom().item(Result.conflict("username")) : super.create(o));
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<UserEntity>> update(UserEntity o) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfEdit = clientUser.getRole().getAuthorities().contains(Authority.SELF_UPSERT);
        boolean canEditAnyUser = clientUser.getRole().getAuthorities().contains(Authority.X_USER_UPSERT);
        boolean canEditUser = clientUser.getRole().getAuthorities().contains(Authority.USER_UPSERT);
        boolean canEditAnyRole = clientUser.getRole().getAuthorities().contains(Authority.X_ROLE_UPSERT);
        boolean canEditRole = clientUser.getRole().getAuthorities().contains(Authority.ROLE_UPSERT);
        return userRepository.findById(o.getId()).chain(user -> {
            if (user == null) return Uni.createFrom().item(Result.notFound());
            boolean selfCase = user.getId().equals(clientUser.getId()) && canSelfEdit;
            boolean gradeAllowedUser = clientUser.getRole().getGrade() > user.getRole().getGrade() && canEditUser;
            if (!gradeAllowedUser && !canEditAnyUser && !selfCase) return Uni.createFrom().item(Result.forbidden());
            Uni<RoleEntity> roleCheckUni = (o.getRole() == null || o.getRole().getId() == null) ? Uni.createFrom().item(user.getRole()) : roleRepository.findById(o.getRole().getId());
            return roleCheckUni.chain(role -> {
                if (role == null) return Uni.createFrom().item(Result.notFound());
                boolean roleChanged = !role.getId().equals(user.getRole().getId());
                boolean gradeAllowedRole = clientUser.getRole().getGrade() > role.getGrade() && canEditRole;
                if (roleChanged && !gradeAllowedRole && !canEditAnyRole) return Uni.createFrom().item(Result.forbidden());
                if (roleChanged && selfCase && !clientUser.getRole().getId().equals(role.getId())) return Uni.createFrom().item(Result.forbidden());
                Uni<Boolean> usernameExistsUni = (o.getUsername() == null || o.getUsername().equals(user.getUsername())) ? Uni.createFrom().item(false) : userRepository.find("username = ?1 AND id != ?2", o.getUsername(), o.getId()).count().map(c -> c > 0);
                return usernameExistsUni.chain(exists -> {
                    if (exists) return Uni.createFrom().item(Result.conflict("username"));
                    o.setPassword(null);
                    o.setCreatedAt(null);
                    return super.update(o);
                });
            });
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<List<UserEntity>>> deleteById(List<UUID> ids) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfDelete = clientUser.getRole().getAuthorities().contains(Authority.SELF_DELETE);
        boolean canDeleteAnyUser = clientUser.getRole().getAuthorities().contains(Authority.X_USER_DELETE);
        boolean canDeleteUser = clientUser.getRole().getAuthorities().contains(Authority.USER_DELETE);
        if (!canSelfDelete && !canDeleteAnyUser && canDeleteUser) return Uni.createFrom().item(Result.forbidden());
        String query = canDeleteAnyUser ? "FROM UserEntity u WHERE u.id IN :ids" : "FROM UserEntity u WHERE u.id IN :ids AND" + (canSelfDelete ? " (u.role.grade < :clientGrade OR u.id = :clientId)" : " u.role.grade < :clientGrade");
        Map<String, Object> parameters = new HashMap<>() {{
            put("ids", ids);
        }};
        if (!canDeleteAnyUser) {
            parameters.put("clientGrade", clientUser.getRole().getGrade());
            if (canSelfDelete) parameters.put("clientId", clientUser.getId());
        }
        return userRepository.find(query, parameters).list().chain(authorizedUsers -> {
            if (authorizedUsers.size() != ids.size()) return Uni.createFrom().item(Result.notFound());
            return userRepository.find("FROM UserEntity u WHERE u.id IN :ids AND (u.programs IS NOT EMPTY OR u.devices IS NOT EMPTY)", parameters).firstResult().chain(relations -> {
                if (relations != null) return Uni.createFrom().item(Result.badRequest("Some users own devices and/or programs, make sure to unlink these entities and try again"));
                return historicalSessionRepository.delete("userOwner.id IN :ids", parameters).chain(() -> super.deleteById(ids));
            });
        });
    }
}
