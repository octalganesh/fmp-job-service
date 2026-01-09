package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickBooksInvoiceResponse {

    @JsonProperty("Invoice")
    private Invoice invoice;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Invoice {

        private String Id;
        private String DocNumber;
        private String TxnDate;
        private Double TotalAmt;
        private Double Balance;
        private String DueDate;

        private CustomerRef CustomerRef;
        private MetaData MetaData;
        private List<Line> Line;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomerRef {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData {
        private String CreateTime;
        private String LastUpdatedTime;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Line {
        private SalesItemLineDetail SalesItemLineDetail;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SalesItemLineDetail {
        private ItemRef ItemRef;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemRef {
        private String name;
    }
}

