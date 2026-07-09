package com.egorgoncharov.krot.backend.application.service;

import com.egorgoncharov.krot.backend.application.Result;
import com.egorgoncharov.krot.backend.application.context.common.EntityCreationContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityDeleteContext;
import com.egorgoncharov.krot.backend.application.context.common.EntityUpdateContext;
import com.egorgoncharov.krot.backend.database.Identifiable;
import com.egorgoncharov.krot.backend.database.relational.RelationalCrudRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.List;

public abstract class AbstractModifyingService<T extends Identifiable<I>, I, A> implements ModifyingService<T, I, A> {
    @Inject
    ObjectMapper objectMapper;

    protected abstract RelationalCrudRepository<T, I> repository();

    @Override
    public Uni<Result<T>> create(EntityCreationContext<T, I, A> context) {
        return repository().save(context.getEntity()).map(Result::ok).onFailure().recoverWithItem(err -> Result.conflict());
    }

    @Override
    public Uni<Result<T>> update(EntityUpdateContext<T, I, A> context) {
        return repository().findById(context.getNewEntity().getId()).flatMap(e -> {
            if (e == null) return Uni.createFrom().item(Result.notFound());
            e = merge(e, context.getNewEntity());
            return repository().save(e).map(Result::ok).onFailure().recoverWithItem(err -> Result.conflict());
        });
    }

    @Override
    public Uni<Result<List<T>>> delete(EntityDeleteContext<T, I, A> context) {
        return repository().findById(context.getEntities().stream().map(T::getId).toList()).flatMap(items -> {
            if (items == null || items.isEmpty()) return Uni.createFrom().item(Result.notFound());
            return repository().removeById(items.stream().map(T::getId).toList()).replaceWith(Result.ok(items));
        });
    }

    protected T merge(T base, T patch) {
        try {
            objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL).updateValue(base, patch);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        return base;
    }
}
