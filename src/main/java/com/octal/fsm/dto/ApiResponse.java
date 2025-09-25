package com.octal.fsm.dto;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Date;

//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse implements Serializable {

    private String message;
    private transient Object data;
    private String status;
    private HttpStatus httpStatus;
    private Date timestamp;
    private String path;

    public ApiResponse() {
    }

    public ApiResponse(String message, Object data, String status, HttpStatus httpStatus) {
        this.message = message;
        this.data = data;
        this.status = status;
        this.httpStatus = httpStatus;
    }

    public ApiResponse(String message, Object data, String status, HttpStatus httpStatus, Date timestamp, String path) {
        this.message = message;
        this.data = data;
        this.status = status;
        this.httpStatus = httpStatus;
        this.timestamp = timestamp;
        this.path = path;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}