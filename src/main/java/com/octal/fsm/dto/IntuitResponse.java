package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IntuitResponse {

    @JsonProperty("Customer")
    private Customer customer;

    @JsonProperty("time")
    private String time;

    @Data
    public static class Customer {

        @JsonProperty("domain")
        private String domain;

        @JsonProperty("sparse")
        private boolean sparse;

        @JsonProperty("Id")
        private String id;

        @JsonProperty("SyncToken")
        private String syncToken;

        @JsonProperty("MetaData")
        private MetaData metaData;

        @JsonProperty("FullyQualifiedName")
        private String fullyQualifiedName;

        @JsonProperty("DisplayName")
        private String displayName;

        @JsonProperty("PrintOnCheckName")
        private String printOnCheckName;

        @JsonProperty("Active")
        private boolean active;

        @JsonProperty("DefaultTaxCodeRef")
        private DefaultTaxCodeRef defaultTaxCodeRef;

        @JsonProperty("Taxable")
        private boolean taxable;

        @JsonProperty("Job")
        private boolean job;

        @JsonProperty("BillWithParent")
        private boolean billWithParent;

        @JsonProperty("Balance")
        private double balance;

        @JsonProperty("BalanceWithJobs")
        private double balanceWithJobs;

        @JsonProperty("CurrencyRef")
        private CurrencyRef currencyRef;

        @JsonProperty("PreferredDeliveryMethod")
        private String preferredDeliveryMethod;

        @JsonProperty("IsProject")
        private boolean isProject;
    }

    @Data
    public static class DefaultTaxCodeRef {
        @JsonProperty("value")
        private String value;
    }

    @Data
    public static class MetaData {
        @JsonProperty("CreateTime")
        private String createTime;

        @JsonProperty("LastUpdatedTime")
        private String lastUpdatedTime;
    }

    @Data
    public static class CurrencyRef {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private String value;
    }
}
