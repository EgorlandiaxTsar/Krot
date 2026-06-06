package com.egorgoncharov.krot.backend.service.helper;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class MessageHelper {
    public static String msgOk() {
        return "Action executed with success.";
    }

    public static String msgBadRequest() {
        return "Bad request.";
    }

    public static String msgUnauthorized() {
        return "Login and try again.";
    }

    public static String msgForbidden() {
        return "You don't have authority to do that.";
    }

    public static String msgNotFound() {
        return "Not found.";
    }

    public static String msgNotFound(String object, String identificator, String id) {
        return StringUtils.capitalize(object) + " not found by " + identificator + " '" + id + "'.";
    }

    public static String msgNotFound(String object, String identificator, List<String> ids) {
        return "No " + object + " was found by given " + identificator + "s [" + String.join(",", ids) + "].";
    }

    public static String msgNotFound(String object, String id) {
        return msgNotFound(object, "id", id);
    }

    public static String msgNotFound(String object, List<String> ids) {
        return msgNotFound(object, "id", ids);
    }

    public static String msgConflict() {
        return "Duplicated fields detected.";
    }

    public static String msgConflict(String field, String value) {
        return "Duplicated value '" + value + "' for property '" + field + "'.";
    }

    public static String msgTooManyRequests() {
        return "Too many requests.";
    }

    public static String msgTooManyRequests(int refreshTime) {
        return "Requests rate-limit exceeded, retry again after " + refreshTime + " seconds.";
    }

    public static String msgInternalError() {
        return "Internal server error!";
    }
}
