package com.egorgoncharov.krot.backend.application.query.model;

import com.egorgoncharov.krot.backend.application.query.AbstractApplicationQuery;
import com.egorgoncharov.krot.backend.application.query.filter.TimeRangeFilter;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Getter
public class DeviceQuery extends AbstractApplicationQuery<UUID> {
    private final TimeRangeFilter lastUpdateTime;
    private final TimeRangeFilter creationTime;
    private final UUID ownerId;
    private final String nameQuery;
    private final String addressQuery;
}
