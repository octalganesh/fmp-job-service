package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentListRequestDTO {
    private int pageNumber = 0;
    private int pageSize = 10;
    private String searchText;
    private String sortField = "createdAt";
    private String sortOrder = "desc";
    private Map<String, Object> filters;
}
