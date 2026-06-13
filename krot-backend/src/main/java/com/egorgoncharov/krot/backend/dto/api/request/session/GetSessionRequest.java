package com.egorgoncharov.krot.backend.dto.api.request.session;

import com.egorgoncharov.krot.backend.dto.api.request.RequestWithMetadata;
import com.egorgoncharov.krot.backend.dto.service.filter.TimeRangeFilter;
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
public class GetSessionRequest extends RequestWithMetadata {
    @JsonProperty("ids")
    private List<String> ids;
    @JsonProperty("ownerId")
    private String ownerId;
    @JsonProperty("validUntilTime")
    private TimeRangeFilter validUntilTime;
    @JsonProperty("creationTime")
    private TimeRangeFilter creationTime;
    @NotNull
    @JsonProperty("pagination")
    private PaginationOptions pagination;
}
