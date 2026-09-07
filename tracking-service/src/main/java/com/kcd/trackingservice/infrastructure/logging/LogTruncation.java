package com.kcd.trackingservice.infrastructure.logging;

/**
 * Trunca bodies antes de loguearlos para evitar logs desproporcionadamente
 * grandes (ej. payloads con listas largas).
 */
public final class LogTruncation {

    private static final int DEFAULT_MAX_LENGTH = 2000;
    private static final String TRUNCATED_SUFFIX = "...[truncated]";

    private LogTruncation() {
    }

    public static String truncate(String body) {
        return truncate(body, DEFAULT_MAX_LENGTH);
    }

    public static String truncate(String body, int maxLength) {
        if (body == null || body.length() <= maxLength) {
            return body;
        }
        return body.substring(0, maxLength) + TRUNCATED_SUFFIX;
    }
}
