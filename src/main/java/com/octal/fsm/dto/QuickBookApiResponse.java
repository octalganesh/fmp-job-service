package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickBookApiResponse<T> {
    private int status;
    private boolean success;
    private String message;
    private T data;
}
