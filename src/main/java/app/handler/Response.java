package app.handler;

import java.time.Instant;

public record Response(
        Integer status,
        String error,
        Instant date) {
}
