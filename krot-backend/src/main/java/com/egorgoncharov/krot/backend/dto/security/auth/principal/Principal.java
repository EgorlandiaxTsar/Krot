package com.egorgoncharov.krot.backend.dto.security.auth.principal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Principal {
    @Getter
    private PrincipalType type;
    private Object principal;

    @SuppressWarnings("unchecked")
    public <T> T getPrincipal() {
        return (T) principal;
    }
}
