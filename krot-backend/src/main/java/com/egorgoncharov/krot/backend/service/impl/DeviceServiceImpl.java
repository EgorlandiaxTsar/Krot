package com.egorgoncharov.krot.backend.service.impl;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.RangeFilter;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.relational.RelationalReactiveRepository;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceCollaboratorEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.DeviceEntity;
import com.egorgoncharov.krot.backend.model.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.relational.repository.DeviceCollaboratorRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.DeviceRepository;
import com.egorgoncharov.krot.backend.model.relational.repository.UserRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.service.DeviceService;
import com.egorgoncharov.krot.backend.service.common.ReactiveCrudService;
import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class DeviceServiceImpl extends ReactiveCrudService<DeviceEntity, UUID> implements DeviceService {
    private final SecurityIdentity client;
    private final DeviceRepository deviceRepository;
    private final DeviceCollaboratorRepository deviceCollaboratorRepository;
    private final UserRepository userRepository;

    @Inject
    public DeviceServiceImpl(SecurityIdentity client, DeviceRepository deviceRepository, DeviceCollaboratorRepository deviceCollaboratorRepository, UserRepository userRepository) {
        this.client = client;
        this.deviceRepository = deviceRepository;
        this.deviceCollaboratorRepository = deviceCollaboratorRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected RelationalReactiveRepository<DeviceEntity, UUID> repository() {
        return deviceRepository;
    }

    @Override
    public Uni<Result<Page<DeviceEntity>>> filter(List<UUID> ids, TimeRangeFilter lastUpdateTime, TimeRangeFilter creationTime, UUID ownerId, String nameQuery, String addressQuery, PaginationOptions pagination) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> parameters = new HashMap<>();
        if (!clientUser.getRole().getAuthorities().contains(Authority.X_DEVICE_READ)) {
            if (clientUser.getRole().getAuthorities().contains(Authority.DEVICE_READ)) {
                query.append(" AND (owner.role.grade <= :clientGrade");
                parameters.put("clientGrade", clientUser.getRole().getGrade());
            } else if (clientUser.getRole().getAuthorities().contains(Authority.SELF_READ)) {
                query.append(" AND (owner.id = clientId");
                parameters.put("clientId", clientUser.getId());
            } else {
                return Uni.createFrom().item(Result.forbidden());
            }
            query.append(" OR EXISTS (SELECT c FROM DeviceCollaborator c WHERE c.device = id AND c.collaborator.id = :clientId))");
        }
        if (ids != null && !ids.isEmpty()) {
            query.append(" AND id IN :ids");
            parameters.put("ids", ids);
        }
        if (ownerId != null) {
            query.append(" AND owner.id = :ownerId");
            parameters.put("ownerId", ownerId.toString());
        }
        RangeFilter.applyRangeFilter(query, "lastUpdated", parameters, lastUpdateTime);
        RangeFilter.applyRangeFilter(query, "createdAt", parameters, creationTime);
        if (nameQuery != null) {
            query.append(" AND (lower(name)) LIKE :name");
            parameters.put("name", "%" + nameQuery.toLowerCase() + "%");
        }
        if (addressQuery != null) {
            query.append(" AND (lower(address)) LIKE :address");
            parameters.put("address", "%" + addressQuery.toLowerCase() + "%");
        }
        return executeFilter(query.toString(), parameters, pagination).map(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return result;
            result.getResult().get().getItems().forEach(device -> {
                DeviceCollaboratorEntity collaborator = device.getCollaborators().stream().filter(c -> c.getCollaborator().getId().equals(Objects.requireNonNull(clientUser).getId())).findFirst().orElse(null);
                if (collaborator != null) {
                    if (!collaborator.getCanReadPassword()) device.setPassword(null);
                    if (!collaborator.getCanReadAddress()) device.setAddress(null);
                    if (!collaborator.getCanReadLastUpdate()) device.setLastCalled(null);
                }
            });
            return result;
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<DeviceCollaboratorEntity>> upsertCollaborator(DeviceCollaboratorEntity collaborator) {
        return modifyDevice(collaborator.getDevice().getId()).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.nullCast());
            if (Objects.requireNonNull(SecurityHelper.securityIdentityUser(client)).getId().equals(collaborator.getId())) return Uni.createFrom().item(Result.badRequest("Cannot set owner as a collaborator"));
            DeviceCollaboratorEntity existingCollaborator = result.getResult().get().getCollaborators().stream().filter(e -> e.getCollaborator().getId().equals(collaborator.getCollaborator().getId())).findAny().orElse(null);
            return deviceCollaboratorRepository.save(collaborator).chain(newCollaborator -> {
                Uni<Void> deleteCollaboratorUni = existingCollaborator != null ? deviceCollaboratorRepository.deleteById(existingCollaborator.getId()).replaceWithVoid() : Uni.createFrom().voidItem();
                return deleteCollaboratorUni.chain(() -> Uni.createFrom().item(Result.ok(newCollaborator)));
            });
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<DeviceCollaboratorEntity>> deleteCollaborator(DeviceCollaboratorEntity collaborator) {
        return modifyDevice(collaborator.getDevice().getId()).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.nullCast());
            return deviceCollaboratorRepository.removeById(collaborator.getId()).map(Result::ok);
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<DeviceEntity>> create(DeviceEntity o) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfEdit = clientUser.getRole().getAuthorities().contains(Authority.SELF_UPSERT);
        boolean canEditAnyDevice = clientUser.getRole().getAuthorities().contains(Authority.X_DEVICE_UPSERT);
        boolean canEditDevice = clientUser.getRole().getAuthorities().contains(Authority.DEVICE_UPSERT);
        if (!canEditDevice && !canEditAnyDevice && !canSelfEdit) return Uni.createFrom().item(Result.forbidden());
        OffsetDateTime now = OffsetDateTime.now();
        o.setAddress("0.0.0.0");
        o.setLastCalled(now);
        o.setCreatedAt(now);
        o.setOwner(UserEntity.builder().id(clientUser.getId()).build());
        return deviceRepository.existsByName(o.getName()).chain(exists -> exists ? Uni.createFrom().item(Result.conflict("name")) : super.create(o));
    }

    @WithTransaction
    @Override
    public Uni<Result<DeviceEntity>> update(DeviceEntity o) {
        return modifyDevice(o.getId(), true, false).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.nullCast());
            DeviceEntity device = result.getResult().get();
            UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
            DeviceCollaboratorEntity collaborator = device.getCollaborators().stream().filter(c -> c.getCollaborator().getId().equals(Objects.requireNonNull(clientUser).getId())).findFirst().orElse(null);
            if (collaborator != null) {
                if (!o.getName().equals(device.getName()) && !collaborator.getCanUpdateName()) return Uni.createFrom().item(Result.forbidden());
                if (!o.getPassword().equals(device.getPassword()) && !collaborator.getCanUpdatePassword()) return Uni.createFrom().item(Result.forbidden());
            }
            Uni<Boolean> nameExistsUni = (o.getName() == null || o.getName().equals(device.getName())) ? Uni.createFrom().item(false) : deviceRepository.find("name = ?1 AND id != ?2", o.getName(), o.getId()).count().map(c -> c > 0);
            return nameExistsUni.chain(exists -> {
                if (exists) return Uni.createFrom().item(Result.conflict("name"));
                o.setOwner(null);
                o.setCreatedAt(null);
                return super.update(o);
            });
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<Void>> transferOwnership(UUID deviceId, UUID newOwnerId) {
        return modifyDevice(deviceId, false, true).chain(result -> {
            if (result.getCode() != 200 || result.getResult().isEmpty()) return Uni.createFrom().item(result.nullCast());
            DeviceEntity device = result.getResult().get();
            device.getCollaborators().removeIf(collaborator -> collaborator.getCollaborator().getId().equals(newOwnerId));
            return userRepository.existsById(newOwnerId).chain(exists -> {
                if (!exists) return Uni.createFrom().item(Result.notFound().voidCast());
                device.setOwner(UserEntity.builder().id(newOwnerId).build());
                device.setName(null);
                device.setPassword(null);
                device.setLastCalled(null);
                device.setCreatedAt(null);
                return deviceRepository.save(device).replaceWith(Result.ok().voidCast());
            });
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<List<DeviceEntity>>> deleteById(List<UUID> ids) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfUpsert = clientUser.getRole().getAuthorities().contains(Authority.SELF_UPSERT);
        boolean canDeleteAnyDevice = clientUser.getRole().getAuthorities().contains(Authority.X_DEVICE_DELETE);
        boolean canDeleteDevice = clientUser.getRole().getAuthorities().contains(Authority.DEVICE_DELETE);
        if (!canSelfUpsert && !canDeleteAnyDevice && !canDeleteDevice) return Uni.createFrom().item(Result.forbidden());
        String query = canDeleteAnyDevice ? "FROM DeviceEntity d WHERE d.id IN :ids" : "FROM DeviceEntity d WHERE d.id IN :ids AND" + (canSelfUpsert ? " (d.owner.role.grade < :clientGrade OR d.owner.id = :clientId)" : " d.owner.role.grade < :clientGrade");
        Map<String, Object> parameters = new HashMap<>() {{
            put("ids", ids);
        }};
        if (!canDeleteAnyDevice) {
            parameters.put("clientGrade", clientUser.getRole().getGrade());
            if (canSelfUpsert) parameters.put("clientId", clientUser.getId());
        }
        return deviceRepository.find(query, parameters).list().chain(authorizedDevices -> {
            if (authorizedDevices.size() != ids.size()) return Uni.createFrom().item(Result.notFound());
            return deviceRepository.find("FROM DeviceEntity d WHERE d.id IN :ids AND (d.collaborators IS NOT EMPTY)", parameters).firstResult().chain(relations -> {
                if (relations != null) return Uni.createFrom().item(Result.badRequest("Some devices have collaborators, make sure to unlink them and try again"));
                return super.deleteById(ids);
            });
        });
    }

    private Uni<Result<DeviceEntity>> modifyDevice(UUID deviceId, boolean allowCollaborator, boolean requireTransferAuthority) {
        UserEntity clientUser = SecurityHelper.securityIdentityUser(client);
        if (clientUser == null) return Uni.createFrom().item(Result.forbidden());
        boolean canSelfEdit = clientUser.getRole().getAuthorities().contains(Authority.SELF_UPSERT);
        boolean canEditAnyDevice = clientUser.getRole().getAuthorities().contains(Authority.X_DEVICE_UPSERT);
        boolean canEditDevice = clientUser.getRole().getAuthorities().contains(Authority.DEVICE_UPSERT);
        boolean canSelfTransferDevice = clientUser.getRole().getAuthorities().contains(Authority.SELF_DEVICE_TRANSFER_OWNERSHIP);
        boolean canTransferAnyDevice = clientUser.getRole().getAuthorities().contains(Authority.X_DEVICE_TRANSFER_OWNERSHIP);
        boolean canTransferDevice = clientUser.getRole().getAuthorities().contains(Authority.DEVICE_TRANSFER_OWNERSHIP);
        return deviceRepository.findById(deviceId).chain(device -> {
            if (device == null) return Uni.createFrom().item(Result.notFound());
            boolean selfCase = device.getOwner().getId().equals(clientUser.getId()) && canSelfEdit;
            boolean gradeAllowed = clientUser.getRole().getGrade() > device.getOwner().getRole().getGrade() && canEditDevice;
            boolean collaboratorAllowed = allowCollaborator && device.getCollaborators().stream().anyMatch(c -> c.getId().equals(clientUser.getId()));
            boolean canTransferOwnership = !requireTransferAuthority || ((selfCase && canSelfTransferDevice) || (gradeAllowed && canTransferDevice) || canTransferAnyDevice);
            if ((!selfCase && !gradeAllowed && !canEditAnyDevice && !collaboratorAllowed) || !canTransferOwnership) return Uni.createFrom().item(Result.forbidden());
            return Uni.createFrom().item(Result.ok(device));
        });
    }

    private Uni<Result<DeviceEntity>> modifyDevice(UUID deviceId) {
        return modifyDevice(deviceId, false, false);
    }
}
