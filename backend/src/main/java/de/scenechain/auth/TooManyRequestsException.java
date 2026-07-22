package de.scenechain.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class TooManyRequestsException extends ResponseStatusException {
    private final HttpHeaders headers = new HttpHeaders();

    public TooManyRequestsException(int retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, "Request rate exceeded");
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1, retryAfterSeconds)));
    }

    @Override
    public HttpHeaders getHeaders() { return headers; }
}
