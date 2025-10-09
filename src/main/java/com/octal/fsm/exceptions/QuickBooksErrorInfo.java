package com.octal.fsm.exceptions;

import lombok.Data;

@Data
public class QuickBooksErrorInfo {
    private String code;
    private String message;
    private String detail;
    private String raw;

    public QuickBooksErrorInfo() {
    }

    public QuickBooksErrorInfo(String code, String message, String detail, String raw) {
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.raw = raw;
    }
}
