package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    @JsonProperty("Line")
    private List<LineItem> line;

    @JsonProperty("CustomerRef")
    private CustomerRef customerRef;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItem {
        @JsonProperty("Amount")
        private Double amount;

        @JsonProperty("DetailType")
        private String detailType;

        @JsonProperty("SalesItemLineDetail")
        private SalesItemLineDetail salesItemLineDetail;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SalesItemLineDetail {
            @JsonProperty("ItemRef")
            private ItemRef itemRef;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class ItemRef {
                @JsonProperty("value")
                private String value;

                @JsonProperty("name")
                private String name;
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerRef {
        @JsonProperty("value")
        private String value;
    }
}
