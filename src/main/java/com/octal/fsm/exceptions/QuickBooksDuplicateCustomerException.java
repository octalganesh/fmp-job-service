package com.octal.fsm.exceptions;


public class QuickBooksDuplicateCustomerException extends RuntimeException {
    private final QuickBooksErrorInfo errorInfo;

    public QuickBooksDuplicateCustomerException(QuickBooksErrorInfo errorInfo) {
        super(errorInfo != null ? errorInfo.getMessage() : "Duplicate Customer");
        this.errorInfo = errorInfo;
    }

    public QuickBooksErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
