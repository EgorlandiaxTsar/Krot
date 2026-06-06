package com.egorgoncharov.krot.backend.dto.api.request.role;

import com.egorgoncharov.krot.backend.dto.api.request.RequestWithMetadata;
import com.egorgoncharov.krot.backend.dto.service.filter.NumericalRangeFilter;
import com.egorgoncharov.krot.backend.dto.service.pagination.PaginationOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class GetRoleRequest extends RequestWithMetadata {
    @JsonProperty("ids")
    private List<String> ids;
    @JsonProperty("authorities")
    private List<String> authorities;
    @JsonProperty("nameQuery")
    private String nameQuery;
    @JsonProperty("grade")
    private NumericalRangeFilter grade;
    @NotNull
    @JsonProperty("pagination")
    private PaginationOptions pagination;
}
