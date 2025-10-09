package com.octal.fsm.exceptions;

public class QuickBooksClientException extends RuntimeException {
    private final QuickBooksErrorInfo error;

    public QuickBooksClientException(QuickBooksErrorInfo error) {
        super(error != null ? error.getMessage() : "QuickBooks API error");
        this.error = error;
    }

    public QuickBooksErrorInfo getError() {
        return error;
    }
}
