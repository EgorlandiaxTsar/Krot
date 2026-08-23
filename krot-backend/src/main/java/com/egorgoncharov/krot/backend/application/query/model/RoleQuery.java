package com.egorgoncharov.krot.backend.application.query.model;

import com.egorgoncharov.krot.backend.application.query.AbstractApplicationQuery;
import com.egorgoncharov.krot.backend.application.query.filter.NumericalRangeFilter;
import com.egorgoncharov.krot.backend.security.Authority;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@SuperBuilder
@Getter
public class RoleQuery extends AbstractApplicationQuery<UUID> {
    private final List<Authority> authorities;
    private final String nameQuery;
    private final NumericalRangeFilter grade;
}
