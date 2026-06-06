package com.egorgoncharov.krot.backend.dto.api.request.role;

import com.egorgoncharov.krot.backend.dto.api.request.RequestWithMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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
public class EditRoleRequest extends RequestWithMetadata {
    @NotNull
    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    @JsonProperty("id")
    private String id;
    @Size(min = 3, max = 32)
    @JsonProperty("name")
    private String name;
    @Min(0)
    @JsonProperty("grade")
    private int grade;
    @JsonProperty("authorities")
    private List<String> authorities;
}
