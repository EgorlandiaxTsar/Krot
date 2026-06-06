package com.egorgoncharov.krot.backend.service.impl;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.filter.NumericalRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.filter.RangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.common.ReactiveRepository;
import com.egorgoncharov.krot.backend.model.entity.RoleEntity;
import com.egorgoncharov.krot.backend.model.entity.UserEntity;
import com.egorgoncharov.krot.backend.model.repository.RoleRepository;
import com.egorgoncharov.krot.backend.security.Authority;
import com.egorgoncharov.krot.backend.service.RoleService;
import com.egorgoncharov.krot.backend.service.common.ReactiveCrudService;
import com.egorgoncharov.krot.backend.service.helper.SecurityHelper;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleServiceImpl extends ReactiveCrudService<RoleEntity, UUID> implements RoleService {
    private final SecurityIdentity client;
    private final RoleRepository roleRepository;

    @Inject
    public RoleServiceImpl(SecurityIdentity client, RoleRepository roleRepository) {
        this.client = client;
        this.roleRepository = roleRepository;
    }

    @Override
    protected ReactiveRepository<RoleEntity, UUID> repository() {
        return roleRepository;
    }

    @WithSession
    @Override
    public Uni<Result<Page<RoleEntity>>> filter(List<UUID> ids, List<String> authorities, String nameQuery, NumericalRangeFilter grade, PaginationOptions pagination) {
        RoleEntity clientRole = SecurityHelper.securityIdentityRole(client);
        if (clientRole == null) return Uni.createFrom().item(Result.forbidden());
        StringBuilder query = new StringBuilder("LEFT JOIN FETCH users WHERE 1=1");
        Parameters parameters = new Parameters();
        if (!clientRole.getAuthorities().contains(Authority.X_ROLE_READ)) {
            if (clientRole.getAuthorities().contains(Authority.ROLE_READ)) {
                query.append(" AND grade <= :clientGrade");
                parameters.and("clientGrade", clientRole.getGrade());
            } else if (clientRole.getAuthorities().contains(Authority.SELF_READ)) {
                query.append(" AND grade = :clientGrade");
                parameters.and("clientGrade", clientRole.getGrade());
            } else {
                return Uni.createFrom().item(Result.forbidden());
            }
        }
        if (ids != null && !ids.isEmpty()) {
            query.append(" AND id IN :ids");
            parameters.and("ids", ids);
        }
        if (authorities != null && !authorities.isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM RoleEntity r JOIN r.authorities a WHERE r.id = id AND a IN :authorities)");
            parameters.and("authorities", authorities);
        }
        RangeFilter.applyRangeFilter(query, "grade", parameters, grade);
        if (nameQuery != null && !nameQuery.isBlank()) {
            query.append(" AND lower(name) LIKE :name");
            parameters.and("name", "%" + nameQuery.toLowerCase() + "%");
        }
        return executeFilter(query.toString(), parameters, pagination);
    }

    @WithSession
    @Override
    public Uni<Result<Map<RoleEntity, List<UserEntity>>>> findAssignedUsers(List<UUID> ids) {
        RoleEntity clientRole = SecurityHelper.securityIdentityRole(client);
        if (clientRole == null) return Uni.createFrom().item(Result.forbidden());
        boolean canReadAnyRole = clientRole.getAuthorities().contains(Authority.X_ROLE_READ);
        boolean canReadRole = clientRole.getAuthorities().contains(Authority.ROLE_READ);
        boolean canReadAnyUser = clientRole.getAuthorities().contains(Authority.X_USER_READ);
        boolean canReadUser = clientRole.getAuthorities().contains(Authority.USER_READ);
        if (!canReadAnyRole && !canReadRole && !canReadAnyUser && !canReadUser) return Uni.createFrom().item(Result.forbidden());
        String roleFilter = canReadAnyRole ? "r.id IN :ids" : "r.id IN :ids AND r.grade < :clientGrade";
        StringBuilder query = new StringBuilder("FROM RoleEntity r LEFT JOIN FETCH r.users u");
        query.append(" WHERE ").append(roleFilter);
        if (!canReadAnyUser) query.append(" AND u.role.grade < :clientGrade");
        Parameters parameters = Parameters.with("ids", ids);
        if (!canReadAnyRole || !canReadAnyUser) parameters.and("clientGrade", clientRole.getGrade());
        return roleRepository.find(query.toString(), parameters.map()).list().map(items -> {
            Map<RoleEntity, List<UserEntity>> roleUsersMap = new HashMap<>();
            items.forEach(e -> roleUsersMap.get(e).addAll(e.getUsers()));
            return Result.ok(roleUsersMap);
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<RoleEntity>> create(RoleEntity o) {
        RoleEntity clientRole = SecurityHelper.securityIdentityRole(client);
        if (clientRole == null) return Uni.createFrom().item(Result.forbidden());
        boolean canEditAnyRole = clientRole.getAuthorities().contains(Authority.X_ROLE_UPSERT);
        boolean canEditRole = clientRole.getAuthorities().contains(Authority.ROLE_UPSERT);
        if (o.getGrade() < 0) return Uni.createFrom().item(Result.badRequest("Role grade must be greater than zero"));
        if (o.getGrade() >= clientRole.getGrade() && !canEditAnyRole) return Uni.createFrom().item(Result.forbidden());
        if (!canEditRole && !canEditAnyRole) return Uni.createFrom().item(Result.forbidden());
        if (o.getAuthorities().stream().map(Enum::name).anyMatch(authority -> authority.startsWith("X")) && !clientRole.getAuthorities().contains(Authority.X_AUTHORITY_MANAGE)) return Uni.createFrom().item(Result.forbidden());
        return roleRepository.existsByName(o.getName()).chain(exists -> exists ? Uni.createFrom().item(Result.conflict("name")) : super.create(o));
    }

    @WithTransaction
    @Override
    public Uni<Result<RoleEntity>> update(RoleEntity o) {
        RoleEntity clientRole = SecurityHelper.securityIdentityRole(client);
        if (clientRole == null) return Uni.createFrom().item(Result.forbidden());
        boolean canEditAnyRole = clientRole.getAuthorities().contains(Authority.X_ROLE_UPSERT);
        boolean canEditRole = clientRole.getAuthorities().contains(Authority.ROLE_UPSERT);
        if (o.getGrade() != null && o.getGrade() < 0) return Uni.createFrom().item(Result.badRequest("Role grade must be greater than zero"));
        return roleRepository.findById(o.getId()).chain(role -> {
            if (role == null) return Uni.createFrom().item(Result.notFound());
            boolean gradeAllowed = clientRole.getGrade() > role.getGrade() && canEditRole;
            if (!gradeAllowed && !canEditAnyRole) return Uni.createFrom().item(Result.forbidden());
            if (o.getGrade() != null && o.getGrade() >= clientRole.getGrade() && !canEditAnyRole) return Uni.createFrom().item(Result.forbidden());
            if (o.getAuthorities() != null) {
                Set<Authority> oldX = role.getAuthorities().stream().filter(a -> a.name().startsWith("X")).collect(Collectors.toSet());
                Set<Authority> newX = o.getAuthorities().stream().filter(a -> a.name().startsWith("X")).collect(Collectors.toSet());
                if (!oldX.equals(newX) && !clientRole.getAuthorities().contains(Authority.X_AUTHORITY_MANAGE)) return Uni.createFrom().item(Result.forbidden());
            }
            Uni<Boolean> nameExistsUni = (o.getName() == null || o.getName().equals(role.getName())) ? Uni.createFrom().item(false) : roleRepository.find("name = ?1 AND id != ?2", o.getName(), o.getId()).firstResult().map(Objects::nonNull);
            return nameExistsUni.chain(exists -> {
                if (exists) return Uni.createFrom().item(Result.conflict("name"));
                return super.update(o);
            });
        });
    }

    @WithTransaction
    @Override
    public Uni<Result<List<RoleEntity>>> deleteById(List<UUID> ids) {
        RoleEntity clientRole = SecurityHelper.securityIdentityRole(client);
        if (clientRole == null) return Uni.createFrom().item(Result.forbidden());
        boolean canDeleteAnyRole = clientRole.getAuthorities().contains(Authority.X_ROLE_DELETE);
        boolean canDeleteRole = clientRole.getAuthorities().contains(Authority.ROLE_DELETE);
        if (!canDeleteAnyRole && !canDeleteRole) return Uni.createFrom().item(Result.forbidden());
        String query = canDeleteAnyRole ? "FROM RoleEntity r WHERE r.id IN :ids" : "FROM RoleEntity r WHERE r.id IN :ids AND r.grade < :clientGrade";
        Parameters parameters = new Parameters().and("ids", ids);
        if (!canDeleteAnyRole) parameters.and("clientGrade", clientRole.getGrade());
        return roleRepository.find(query, parameters.map()).list().chain(authorizedRoles -> {
            if (authorizedRoles.size() != ids.size()) return Uni.createFrom().item(Result.notFound());
            return roleRepository.find("FROM RoleEntity r WHERE r.id IN :ids AND u.users IS NOT EMPTY", parameters.map()).firstResult().chain(relations -> {
                if (relations != null) return Uni.createFrom().item(Result.badRequest("Some roles contain users, make sure to assign new roles to current users and try again"));
                return super.deleteById(ids);
            });
        });
    }
}
