package com.octal.fsm.exceptions;

public class RestClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int httpStatus;
    private final String message;

    public RestClientException() {
        super();
        this.httpStatus = 0;
        this.message = null;
    }

    public RestClientException(String message) {
        super(message);
        this.httpStatus = 0;
        this.message = message;
    }

    public RestClientException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 0;
        this.message = message;
    }

    public RestClientException(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
