package com.octal.fsm.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private Boolean success;
    private String message;
    private  Object data;
    private String status;
    private HttpStatus httpStatus;


    public ApiResponse(String message, Object data, String status, HttpStatus httpStatus) {
        this.message = message;
        this.data = data;
        this.status = status;
        this.httpStatus = httpStatus;
    }
}
