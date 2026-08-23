package com.egorgoncharov.krot.backend.api.model.request.program;

import com.egorgoncharov.krot.backend.api.model.request.RequestWithMetadata;
import com.egorgoncharov.krot.backend.application.query.filter.TimeRangeFilter;
import com.egorgoncharov.krot.backend.application.query.pagination.PaginationOptions;
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
public class GetProgramRequest extends RequestWithMetadata {
    @JsonProperty("ids")
    private List<String> ids;
    @JsonProperty("creationTime")
    private TimeRangeFilter creationTime;
    @JsonProperty
    private String ownerId;
    @JsonProperty("nameQuery")
    private String nameQuery;
    @NotNull
    @JsonProperty("pagination")
    private PaginationOptions pagination;
}
