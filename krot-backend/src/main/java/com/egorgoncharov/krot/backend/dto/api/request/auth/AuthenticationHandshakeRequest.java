package com.egorgoncharov.krot.backend.dto.api.request.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AuthenticationHandshakeRequest {
    @NotNull
    @NotBlank
    @Size(min = 3, max = 32)
    @JsonProperty("identifier")
    private String identifier;
    @NotNull
    @Size(min = 8, max = 32)
    @JsonProperty("password")
    private String password;
    @NotNull
    @Min(0)
    @JsonProperty("timestamp")
    private Long timestamp;
}
