package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String jobId;
    private String jobType;
    private String paymentId;
    private Double totalPaymentAmount;
    private LocalDate paymentReceivedDate;
}
