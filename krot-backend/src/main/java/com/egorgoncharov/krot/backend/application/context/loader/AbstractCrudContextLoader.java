package com.egorgoncharov.krot.backend.application.context.loader;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityQueryContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.application.query.AbstractApplicationQuery;
import com.egorgoncharov.krot.backend.application.query.pagination.Page;
import com.egorgoncharov.krot.backend.database.Identifiable;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.egorgoncharov.krot.backend.database.relational.entity.UserEntity;
import com.egorgoncharov.krot.backend.security.session.principal.PrincipalExtractor;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

public abstract class AbstractCrudContextLoader<T extends Identifiable<I>, I, A, Q extends AbstractApplicationQuery<I>> implements CrudContextLoader<T, I, A, Q> {
    @Inject
    protected CurrentIdentityAssociation currentIdentityAssociation;

    protected abstract RelationalCrudRepository<T, I> repository();

    @Override
    public Uni<Result<? extends EntityCreationContext<T, I, A>>> creationContext(T entity, A subcontext) {
        return currentIdentityAssociation.getDeferredIdentity().chain(identity -> {
            UserEntity principal = PrincipalExtractor.principal(identity);
            if (principal == null) return Result.<EntityCreationContext<T, I, A>>forbidden().toUni();
            EntityCreationContext<T, I, A> context = EntityCreationContext.<T, I, A>builder().entity(entity).principal(principal).additionalContext(null).build();
            return Result.ok(context).toUni();
        });
    }

    @Override
    public Uni<Result<? extends EntityUpdateContext<T, I, A>>> updateContext(T entity, A subcontext) {
        return currentIdentityAssociation.getDeferredIdentity().chain(identity -> {
            UserEntity principal = PrincipalExtractor.principal(identity);
            if (principal == null) return Result.<EntityUpdateContext<T, I, A>>forbidden().toUni();
            return repository().findById(entity.getId()).chain(existing -> {
                if (existing == null) return Result.<EntityUpdateContext<T, I, A>>notFound().toUni();
                EntityUpdateContext<T, I, A> context = EntityUpdateContext.<T, I, A>builder().oldEntity(existing).newEntity(entity).principal(principal).additionalContext(null).build();
                return Result.ok(context).toUni();
            });
        });
    }

    @Override
    public Uni<Result<? extends EntityDeleteContext<T, I, A>>> deleteContext(List<T> entities, A subcontext) {
        return currentIdentityAssociation.getDeferredIdentity().chain(identity -> {
            UserEntity principal = PrincipalExtractor.principal(identity);
            if (principal == null) return Result.<EntityDeleteContext<T, I, A>>forbidden().toUni();
            return repository().findById(entities.stream().map(T::getId).toList()).chain(items -> {
                if (items.size() != entities.size()) return Result.<EntityDeleteContext<T, I, A>>notFound().toUni();
                EntityDeleteContext<T, I, A> context = EntityDeleteContext.<T, I, A>builder().entities(entities).principal(principal).additionalContext(null).build();
                return Result.ok(context).toUni();
            });
        });
    }

    protected Uni<Result<? extends EntityQueryContext<T, I, A>>> executeQuery(String sql, Map<String, Object> parameters, Q query) {
        return currentIdentityAssociation.getDeferredIdentity().chain(identity -> {
            UserEntity principal = PrincipalExtractor.principal(identity);
            if (principal == null) return Result.<EntityQueryContext<T, I, A>>forbidden().toUni();
            return repository()
                    .find(sql, parameters)
                    .page(io.quarkus.panache.common.Page.of(query.getPagination().getPage(), query.getPagination().getLimit())).list()
                    .chain(items -> repository().count(sql, parameters).map(count -> new Page<>(items, count, query.getPagination().getLimit(), query.getPagination().getPage())))
                    .flatMap(page -> Result.ok(EntityQueryContext.<T, I, A>builder().results(page).principal(principal).additionalContext(null).build()).toUni());
        });
    }
}
