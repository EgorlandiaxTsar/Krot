package com.egorgoncharov.krot.backend.dto.api.request;

import com.egorgoncharov.krot.backend.dto.api.ApiRequestMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class RequestWithMetadata {
    @NotNull
    @JsonProperty("metadata")
    private ApiRequestMetadata metadata;
}
