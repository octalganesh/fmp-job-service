package com.octal.fsm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class TransactionResponseDTO {

    private List<PaymentResponseDTO> items;
    private Integer totalPages;
    private Long totalItems;
    private Integer pageNumber;
    private Integer pageSize;
    private Double totalAmount;
}
