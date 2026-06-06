package com.egorgoncharov.krot.backend.service.helper;

import com.egorgoncharov.krot.backend.security.Authority;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.UUID;

public class TypesHelper {
    public static boolean validateUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean validateUUID(List<String> uuids) {
        boolean isValid = true;
        for (String u : uuids) {
            isValid = validateUUID(u);
            if (!isValid) break;
        }
        return isValid;
    }

    public static boolean validateAuthorities(List<String> authorities) {
        boolean isValid = true;
        for (String a : authorities) {
            try {
                Authority.valueOf(a);
            } catch (IllegalArgumentException e) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public static List<UUID> toUUID(List<String> uuids) {
        return uuids == null ? null : uuids.stream().map(UUID::fromString).toList();
    }

    public static UUID toUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }

    public static List<Authority> toAuthoritiesList(List<String> authorities) {
        return authorities == null ? null : authorities.stream().map(Authority::valueOf).toList();
    }

    public static Sort toSort(String sort) {
        if (sort == null || sort.replaceFirst("-", "").isBlank()) return Sort.by("id");
        boolean desc = sort.startsWith("-");
        sort = desc ? sort.substring(1) : sort;
        return desc ? Sort.descending(sort) : Sort.ascending(sort);
    }
}
