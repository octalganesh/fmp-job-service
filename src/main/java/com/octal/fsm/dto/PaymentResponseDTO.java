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

    private String id;                  // JobInvoice.id
    private String jobId;
    private String taskId;
    private String jobTypeId;
    private String paymentId;
    private Double totalPaymentAmount;
    private Double totalPaymentReceived;
    private Double totalPaymentPending;
    private Double totalTransactions;
    private Double TotalAmount;
    private String paymentReceivedDate;
    private String paymentStatus;        // PAID / PENDING
    private String receivedPaymentMethod;
    private String customerName;
    private String customerTypeId;
    private String createdAt;
    private String updatedAt;
    private String notes;
    private String location;
}
