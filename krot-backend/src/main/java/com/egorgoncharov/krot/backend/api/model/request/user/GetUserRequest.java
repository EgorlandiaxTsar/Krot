package com.egorgoncharov.krot.backend.api.model.request.user;

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
public class GetUserRequest extends RequestWithMetadata {
    @JsonProperty("ids")
    private List<String> ids;
    @JsonProperty("active")
    private Boolean active;
    @JsonProperty("creationTime")
    private TimeRangeFilter creationTime;
    @JsonProperty("roleId")
    private String roleId;
    @JsonProperty("usernameQuery")
    private String usernameQuery;
    @NotNull
    @JsonProperty("pagination")
    private PaginationOptions pagination;
}
