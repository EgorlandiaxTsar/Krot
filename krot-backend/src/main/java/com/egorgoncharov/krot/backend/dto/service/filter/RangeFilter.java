package com.egorgoncharov.krot.backend.dto.service.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.panache.common.Parameters;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.ALWAYS)
public class RangeFilter<T> {
    @JsonProperty("min")
    private T min;
    @JsonProperty("max")
    private T max;

    public static <T extends RangeFilter<?>> void applyNullableRangeFilter(StringBuilder query, String field, Parameters parameters, NullableValueFilter<T> filter) {
        if (filter == null) return;
        if (!filter.isRequirePresence()) {
            query.append(" AND ").append(field).append(" IS NULL");
        } else {
            if (filter.getFilter() != null && filter.getFilter().isCorrect()) {
                applyRangeFilter(query, field, parameters, filter.getFilter());
            } else {
                query.append(" AND ").append(field).append(" IS NOT NULL");
            }
        }
    }

    public static void applyNullableFilter(StringBuilder query, String field, Parameters parameters, NullableValueFilter<?> filter) {
        if (filter == null) return;
        if (!filter.isRequirePresence()) {
            query.append(" AND ").append(field).append(" IS NULL");
        } else {
            if (filter.getFilter() != null) {
                query.append(" AND ").append(field).append(" = :").append(field);
                parameters.and(field, filter.getFilter());
            } else {
                query.append(" AND ").append(field).append(" IS NOT NULL");
            }
        }
    }

    public static void applyRangeFilter(StringBuilder query, String field, Parameters parameters, RangeFilter<?> filter) {
        if (filter == null) return;
        if (filter.isCorrect()) {
            if (filter.getMin() != null) {
                String minValueName = "min" + StringUtils.capitalize(field);
                query.append(" AND ").append(field).append(" >= :").append(minValueName);
                parameters.and(minValueName, filter.getMin());
            }
            if (filter.getMax() != null) {
                String maxValueName = "min" + StringUtils.capitalize(field);
                query.append(" AND ").append(field).append(" >= :").append(maxValueName);
                parameters.and(maxValueName, filter.getMax());
            }
        }
    }

    public boolean isCorrect() {
        Double min = this.min == null ? null : Double.valueOf(this.min.toString());
        Double max = this.max == null ? null : Double.valueOf(this.max.toString());
        return min == null || max == null || min < max;
    }
}
