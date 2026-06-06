package com.egorgoncharov.krot.backend.dto.service;

import com.egorgoncharov.krot.backend.dto.api.ApiResponse;
import com.egorgoncharov.krot.backend.dto.api.ApiResponseMetadata;
import com.egorgoncharov.krot.backend.service.helper.MessageHelper;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder
public class Result<T> {
    private final Optional<T> result;
    private final String message;
    private final int code;
    private final long timestamp;

    public static <T> Result<T> from(T result, String message, int code) {
        return new Result<>(result == null ? Optional.empty() : Optional.of(result), message, code, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> ok(T result) {
        return Result.from(result, MessageHelper.msgOk(), 200);
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> badRequest() {
        return Result.from(null, MessageHelper.msgBadRequest(), 400);
    }

    public static <T> Result<T> badRequest(String message) {
        return Result.from(null, message, 400);
    }

    public static <T> Result<T> notFound() {
        return Result.from(null, MessageHelper.msgNotFound(), 404);
    }

    public static <T> Result<T> unauthorized() {
        return Result.from(null, MessageHelper.msgUnauthorized(), 401);
    }

    public static <T> Result<T> forbidden() {
        return Result.from(null, MessageHelper.msgForbidden(), 403);
    }

    public static <T> Result<T> conflict() {
        return Result.from(null, MessageHelper.msgConflict(), 409);
    }

    public static <T> Result<T> conflict(String field) {
        return Result.from(null, "Conflict: " + field + " already exists", 409);
    }

    public static <T> Result<T> error() {
        return Result.from(null, MessageHelper.msgInternalError(), 500);
    }

    public ApiResponseMetadata toApiMetadata() {
        return new ApiResponseMetadata(result.isPresent() || code >= 200 && code < 400, message, code, timestamp);
    }

    public ApiResponse<T> toApiResponse() {
        return new ApiResponse<>(toApiMetadata(), null, null);
    }

    public RestResponse<ApiResponse<T>> toRestResponse() {
        return toApiResponse().toRestResponse();
    }

//    public ResponseEntity<MetadataResponse> toSimpleMetadataResponse() {
//        return new ResponseEntity<>(this.toApiMetadata(), HttpStatusCode.valueOf(code));
//    }

    public Uni<Result<T>> toUni() {
        return Uni.createFrom().item(this);
    }

    public Result<Void> voidCast() {
        return Result.from(null, message, code);
    }

    public <E> Result<E> nullCast() {
        return Result.from(null, message, code);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Result) obj;
        return Objects.equals(this.result, that.result) && Objects.equals(this.message, that.message) && this.code == that.code && this.timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, message, code, timestamp);
    }

    @Override
    public String toString() {
        return "ServiceResult[" + "result=" + result + ", " + "message=" + message + ", " + "code=" + code + ", " + "timestamp=" + timestamp + ']';
    }
}
