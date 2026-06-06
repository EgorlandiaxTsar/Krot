package com.egorgoncharov.krot.backend.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponseMetadata {
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("message")
    private String message;
    @JsonProperty("code")
    private int code;
    @JsonProperty("timestamp")
    private long timestamp;
}
