package com.egorgoncharov.krot.backend.application.core.role;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.loader.AbstractCrudContextLoader;
import com.egorgoncharov.krot.backend.application.query.filter.RangeFilter;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.RoleEntity;
import com.egorgoncharov.krot.backend.database.relational.repository.RoleRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class RoleContextLoader extends AbstractCrudContextLoader<RoleEntity, UUID, Void, RoleQuery> {
    private final RoleRepository repository;

    @Inject
    public RoleContextLoader(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    protected RelationalCrudRepository<RoleEntity, UUID> repository() {
        return repository;
    }

    @Override
    public Uni<Result<? extends EntityQueryContext<RoleEntity, UUID, Void>>> queryContext(RoleQuery query, Void subcontext) {
        StringBuilder queryBuilder = new StringBuilder("LEFT JOIN FETCH users WHERE 1=1");
        Map<String, Object> parameters = new HashMap<>();
        if (query.getIds() != null && !query.getIds().isEmpty()) {
            queryBuilder.append(" AND id IN :ids");
            parameters.put("ids", query.getIds());
        }
        if (query.getAuthorities() != null && !query.getAuthorities().isEmpty()) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM RoleEntity r JOIN r.authorities a WHERE r.id = id AND a IN :authorities)");
            parameters.put("authorities", query.getAuthorities().stream().map(Enum::name).toList());
        }
        RangeFilter.applyRangeFilter(queryBuilder, "grade", parameters, query.getGrade());
        if (query.getNameQuery() != null && !query.getNameQuery().isBlank()) {
            queryBuilder.append(" AND lower(name) LIKE :name");
            parameters.put("name", "%" + query.getNameQuery().toLowerCase() + "%");
        }
        return executeQuery(queryBuilder.toString(), parameters, query);
    }
}
