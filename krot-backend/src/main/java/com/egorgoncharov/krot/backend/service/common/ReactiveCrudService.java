package com.egorgoncharov.krot.backend.service.common;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.egorgoncharov.krot.backend.model.common.Identifiable;
import com.egorgoncharov.krot.backend.model.common.ReactiveRepository;
import com.egorgoncharov.krot.backend.service.helper.MessageHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.List;

public abstract class ReactiveCrudService<T extends Identifiable<I>, I> implements CrudService<T, I> {
    @Inject
    ObjectMapper objectMapper;

    protected abstract ReactiveRepository<T, I> repository();

    @Override
    public Uni<Result<T>> find(I id) {
        return repository().findById(id).map(e -> e == null ? Result.notFound() : Result.from(e, MessageHelper.msgOk(), 200));
    }

    @Override
    public Uni<Result<T>> create(T o) {
        return repository().save(o).map(Result::ok).onFailure().recoverWithItem(err -> Result.conflict());
    }

    @Override
    public Uni<Result<T>> update(T o) {
        return repository().findById(o.getId()).flatMap(e -> {
            if (e == null) return Uni.createFrom().item(Result.notFound());
            e = patchMerge(e, o);
            return repository().save(e).map(Result::ok).onFailure().recoverWithItem(err -> Result.conflict());
        });
    }

    @Override
    public Uni<Result<List<T>>> deleteById(List<I> ids) {
        return repository().findById(ids).flatMap(items -> {
            if (items == null || items.isEmpty()) return Uni.createFrom().item(Result.notFound());
            return repository().removeById(items.stream().map(T::getId).toList()).replaceWith(Result.ok(items));
        });
    }

    protected Uni<Result<Page<T>>> executeFilter(String query, Parameters parameters, PaginationOptions pagination) {
        return repository().find(query, parameters.map()).page(io.quarkus.panache.common.Page.of(pagination.getPage(), pagination.getLimit())).list().chain(items -> repository().count(query, parameters.map()).map(count -> Result.ok(new Page<>(items, count, pagination.getLimit(), pagination.getPage()))));
    }

    protected T patchMerge(T base, T patch) {
        try {
            objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL).updateValue(base, patch);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        return base;
    }
}
