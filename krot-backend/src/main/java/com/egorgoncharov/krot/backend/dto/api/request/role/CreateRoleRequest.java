package com.egorgoncharov.krot.backend.dto.api.request.role;

import com.egorgoncharov.krot.backend.dto.api.request.RequestWithMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateRoleRequest extends RequestWithMetadata {
    @NotNull
    @NotBlank
    @Size(min = 3, max = 32)
    @JsonProperty("name")
    private String name;
    @NotNull
    @Min(0)
    @JsonProperty("grade")
    private int grade;
    @NotNull
    @JsonProperty("authorities")
    private List<String> authorities;
}
