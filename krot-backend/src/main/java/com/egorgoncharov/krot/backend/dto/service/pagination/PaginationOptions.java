package com.egorgoncharov.krot.backend.dto.service.pagination;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PaginationOptions {
    @NotNull
    @Min(0)
    @JsonProperty("page")
    private int page;
    @NotNull
    @Min(1)
    @Max(500)
    @JsonProperty("limit")
    private int limit;
    @NotNull
    @JsonProperty("order")
    private String order;
}
