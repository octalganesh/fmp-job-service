package com.octal.fsm.dto;

import lombok.Data;

public class CustomerRestDTO {

    @Data
    public static class CreateQueue {
        private Boolean active;
        private String fullName;
        private String customerId; // customer table record Id from admin service
        private String email;
        private String secondaryEmail;
        private String mobile;
        private String alterNativeMobile1;
        private String alterNativeMobile2;
        private String address;
        private String gender;
        private String customerTypeId;
        private String customerTypeName;
        private String customerUuid;
    }

    @Data
    public static class CallBackEvent {
        private String type; // CUSTOMER, INVOICE and etc
        private String typeId; // customer_uuid, invoice_uuid and etc
        private String quickBookId; // quickbook_customer_list_id, quickbook_invoice_list_id and etc
    }
}
