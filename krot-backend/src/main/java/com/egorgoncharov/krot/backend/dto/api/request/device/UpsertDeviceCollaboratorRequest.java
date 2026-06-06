package com.egorgoncharov.krot.backend.dto.api.request.device;

import com.egorgoncharov.krot.backend.dto.api.request.RequestWithMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class UpsertDeviceCollaboratorRequest extends RequestWithMetadata {
    @NotNull
    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    @JsonProperty("deviceId")
    private String deviceId;
    @NotNull
    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("canReadAddress")
    private Boolean canReadAddress;
    @JsonProperty("canReadPassword")
    private Boolean canReadPassword;
    @JsonProperty("canReadLastUpdate")
    private Boolean canReadLastUpdate;
    @JsonProperty("canUpdateName")
    private Boolean canUpdateName;
    @JsonProperty("canUpdatePassword")
    private Boolean canUpdatePassword;
    @JsonProperty("allowPrograms")
    private List<String> allowPrograms;
}
