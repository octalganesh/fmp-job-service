package com.octal.fsm.dto;

import lombok.Data;

@Data
public class JobInvoiceListDTO {

    private String technicianId;
    private String technicianName;

    private String customerId;
    private String customerName;
    private String jobType;
    private String invoiceStatus;
    private String paymentStatus;

    private String startDate;
    private String endDate;

}
