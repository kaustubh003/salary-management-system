package com.acme.salary.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a leave is added or deleted on a date that a payment already covers.
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class LeaveLockedException extends RuntimeException {
    public LeaveLockedException(String message) {
        super(message);
    }
}
