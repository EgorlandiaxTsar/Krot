package com.egorgoncharov.krot.backend.service.common;

import com.egorgoncharov.krot.backend.dto.service.Result;
import com.egorgoncharov.krot.backend.model.common.Identifiable;
import com.egorgoncharov.krot.backend.service.helper.MutinyHelper;
import io.smallrye.mutiny.Uni;

import java.util.Collections;
import java.util.List;

public interface CrudService<T extends Identifiable<I>, I> {
    Uni<Result<T>> find(I id);

    default Uni<Result<T>> find(T o) {
        return find(o.getId());
    }

    Uni<Result<T>> create(T o);

    Uni<Result<T>> update(T o);

    Uni<Result<List<T>>> deleteById(List<I> ids);

    default Uni<Result<T>> deleteById(I id) {
        return MutinyHelper.singletonUni(deleteById(Collections.singletonList(id)));
    }

    default Uni<Result<List<T>>> delete(List<T> o) {
        return deleteById(o.stream().map(T::getId).toList());
    }

    default Uni<Result<T>> delete(T o) {
        return deleteById(o.getId());
    }
}
