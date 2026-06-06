package com.egorgoncharov.krot.backend.dto.api;

import com.egorgoncharov.krot.backend.dto.service.pagination.Page;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.mutiny.Uni;
import lombok.*;
import org.jboss.resteasy.reactive.RestResponse;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @JsonProperty("metadata")
    private ApiResponseMetadata metadata;
    @JsonProperty("data")
    private T data;
    @JsonProperty("pagination")
    private Page<?> pagination;

    public RestResponse<ApiResponse<T>> toRestResponse() {
        return RestResponse.status(RestResponse.Status.fromStatusCode(metadata.getCode()), this);
    }

    public Uni<RestResponse<ApiResponse<T>>> toUniRestResponse() {
        return Uni.createFrom().item(this::toRestResponse);
    }
}
