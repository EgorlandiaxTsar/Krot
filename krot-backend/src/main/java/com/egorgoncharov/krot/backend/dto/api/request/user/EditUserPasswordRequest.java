package com.egorgoncharov.krot.backend.dto.api.request.user;

import com.egorgoncharov.krot.backend.dto.api.request.RequestWithMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class EditUserPasswordRequest extends RequestWithMetadata {
    @NotNull
    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    @JsonProperty("id")
    private String id;
    @NotNull
    @NotBlank
    @Size(min = 8, max = 32)
    @JsonProperty("password")
    private String password;
    @NotNull
    @NotBlank
    @Size(min = 8, max = 32)
    @JsonProperty("newPassword")
    private String newPassword;
}
