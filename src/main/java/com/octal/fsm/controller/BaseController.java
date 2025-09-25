package com.octal.fsm.controller;


import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.exceptions.CodeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class BaseController {

    private static final Logger logger = LogManager.getLogger(BaseController.class);

    protected ResponseEntity<ApiResponse> handleException(Exception e) {
        if (e instanceof CodeException) {
            return new ResponseEntity<>(new ApiResponse(Boolean.FALSE, e.getMessage(), null, String.valueOf(((CodeException) e).getCode().getCode()), HttpStatus.OK), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse(Boolean.FALSE, e.getMessage(), null, "101", HttpStatus.OK), HttpStatus.OK);
        }
    }



}
