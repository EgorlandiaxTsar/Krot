package com.egorgoncharov.krot.backend.security;

/*
 * Authorities are generally given with a grade-aware philosophy, this means
 * that a user, which might have an authority to execute and action on certain
 * entity, i.e. role, user, device, request, program or session, can do that
 * only if this specific entity (or it's owner) has a grade lower than executor
 * grade. Also, in case of role creation or assignment, the executor role grade
 * must be higher than target role grade.
 *
 * However, authorities starting with an "X" in their name are meant to
 * be exclusive. This means that a user having a role that includes an exclusive
 * authority, will give executor the permission to perform action on ANY entity
 * ignoring the grade.
 *
 * Finally, there are also self-authorities, which permit action execution only
 * on currently logged-in user.
 * */
public enum Authority {
    /*
     * Devices can also be considered as users, but with a fixed and limited set of
     * actions, therefore, they need a special authority that will permit them to
     * execute only that certain actions.
     * */
    DEVICE,

    /* X-AUTHORITIES */
    /* READ */
    X_ROLE_READ, X_USER_READ, X_DEVICE_READ, X_REQUEST_READ, X_PROGRAM_READ, X_SESSION_READ,

    /* UPSERT */
    X_ROLE_UPSERT, X_USER_UPSERT, X_DEVICE_UPSERT, X_PROGRAM_UPSERT,

    /* DELETE */
    X_ROLE_DELETE, X_USER_DELETE, X_DEVICE_DELETE, X_PROGRAM_DELETE, X_SESSION_DELETE,

    /* OWNERSHIP */
    X_DEVICE_TRANSFER_OWNERSHIP, X_PROGRAM_TRANSFER_OWNERSHIP,

    /* SELF-AUTHORITIES */
    SELF_READ, SELF_UPSERT, SELF_UPSERT_PASSWORD, SELF_DELETE, SELF_SESSION_DELETE, SELF_DEVICE_TRANSFER_OWNERSHIP, SELF_PROGRAM_TRANSFER_OWNERSHIP,

    /* AUTHORITIES */
    X_AUTHORITY_MANAGE,

    /* READ */
    ROLE_READ, USER_READ, DEVICE_READ, REQUEST_READ, PROGRAM_READ, SESSION_READ,

    /* UPSERT */
    ROLE_UPSERT, USER_UPSERT, DEVICE_UPSERT, REQUEST_UPSERT, PROGRAM_UPSERT,

    /* DELETE */
    ROLE_DELETE, USER_DELETE, DEVICE_DELETE, REQUEST_DELETE, PROGRAM_DELETE, SESSION_DELETE,

    /* OWNERSHIP */
    DEVICE_TRANSFER_OWNERSHIP, PROGRAM_TRANSFER_OWNERSHIP
}
