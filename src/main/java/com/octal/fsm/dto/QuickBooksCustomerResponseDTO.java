package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickBooksCustomerResponseDTO {

    private int status;
    private boolean success;
    private String message;
    private DataObject data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataObject {
        @JsonProperty("Customer")
        private Customer customer;

        private String time;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
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

        @JsonProperty("BillAddr")
        private BillAddr billAddr;

        @JsonProperty("PrimaryPhone")
        private PrimaryPhone primaryPhone;

        @JsonProperty("PrimaryEmailAddr")
        private PrimaryEmailAddr primaryEmailAddr;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillAddr {
        @JsonProperty("Id")
        private String id;

        @JsonProperty("Line1")
        private String line1;

        @JsonProperty("City")
        private String city;

        @JsonProperty("Country")
        private String country;

        @JsonProperty("CountrySubDivisionCode")
        private String countrySubDivisionCode;

        @JsonProperty("PostalCode")
        private String postalCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryPhone {
        @JsonProperty("FreeFormNumber")
        private String freeFormNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryEmailAddr {
        @JsonProperty("Address")
        private String address;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData {
        @JsonProperty("CreateTime")
        private String createTime;

        @JsonProperty("LastUpdatedTime")
        private String lastUpdatedTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultTaxCodeRef {
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyRef {
        private String name;
        private String value;
    }
}
