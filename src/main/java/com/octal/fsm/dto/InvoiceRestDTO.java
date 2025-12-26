package com.octal.fsm.dto;

import lombok.Data;

@Data
public class InvoiceRestDTO {

    @Data
    public static class Add {
        private String refId;
        private String listId;
        private String customerListId;
        private String customerFullName;
        private String syncStatus;
        private String amount;
        private String createInvoiceJsonResponse;
        private String createInvoiceXmlResponse;
        private String statusCode;
        private String statusSeverity;
        private String statusMessage;
        private String activeToken;
    }

    @Data
    public static class Response {
        private String id;
        private String syncStatus;
        private String statusMessage;
    }
}
