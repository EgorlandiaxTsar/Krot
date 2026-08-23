package com.egorgoncharov.krot.backend.application.query.model;

import com.egorgoncharov.krot.backend.application.query.AbstractApplicationQuery;
import com.egorgoncharov.krot.backend.application.query.filter.TimeRangeFilter;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Getter
public class SessionQuery extends AbstractApplicationQuery<UUID> {
    private final UUID ownerId;
    private final TimeRangeFilter expirationTime;
    private final TimeRangeFilter creationTime;
}
