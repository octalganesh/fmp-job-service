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

    // -------- Identity --------
    private String id;                  // JobInvoice.id
    private String jobId;
    private String taskId;

    // -------- Job --------
    private String jobTypeId;

    // -------- Payment --------
    private String paymentId;           // invoiceId
    private Double totalPaymentAmount;
    private Double totalPaymentReceived;
    private Double totalPaymentPending;
    private String paymentType;//cash/ card/

    private String paymentReceivedDate;
    private String paymentStatus;        // PAID / PENDING
    private String receivedPaymentMethod;

    // -------- Customer --------
    private String customerName;
    private String customerTypeId;

    // -------- Audit --------
    private String createdAt;
    private String updatedAt;

    // -------- Optional --------
    private String notes;
}
