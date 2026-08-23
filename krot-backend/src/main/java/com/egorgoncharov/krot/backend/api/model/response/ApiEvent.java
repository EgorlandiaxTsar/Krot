package com.egorgoncharov.krot.backend.api.model.response;

import com.egorgoncharov.krot.backend.api.model.request.EntityReflection;
import com.egorgoncharov.krot.backend.application.events.EventType;
import com.egorgoncharov.krot.backend.database.Identifiable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiEvent<T extends Identifiable<I>, I, A> {
    @JsonProperty("event")
    private EventType event;
    @JsonProperty("extra")
    private A additionalContext;
    @JsonProperty("targets")
    private List<EntityReflection<T>> targets;
}
