package com.egorgoncharov.krot.backend.dto.api.response;

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
public class AuthenticationCredentials {
    @JsonProperty("sessionReference")
    private String sessionReference;
    @JsonProperty("sessionId")
    private String sessionId;
    @JsonProperty("key")
    private String encryptionKey;
    @JsonProperty("expirationTimestamp")
    private Long validUntil;
}
