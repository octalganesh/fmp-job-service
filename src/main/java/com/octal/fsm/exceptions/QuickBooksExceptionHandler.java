package com.octal.fsm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class QuickBooksExceptionHandler {

    @ExceptionHandler(QuickBooksDuplicateCustomerException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCustomer(QuickBooksDuplicateCustomerException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Duplicate Customer");
        body.put("message", "A customer with this name already exists in QuickBooks.");
        body.put("code", ex.getErrorInfo() != null ? ex.getErrorInfo().getCode() : "6240");
        body.put("quickBooksError", ex.getErrorInfo());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(QuickBooksClientException.class)
    public ResponseEntity<?> handleQuickBooks(QuickBooksClientException ex) {
        QuickBooksErrorInfo err = ex.getError();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("success", false, "error", Map.of("type", "QUICKBOOKS_API_ERROR", "code", err.getCode(), "message", err.getMessage(), "detail", err.getDetail())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", Map.of("type", "INTERNAL_ERROR", "message", ex.getMessage())));
    }
}
