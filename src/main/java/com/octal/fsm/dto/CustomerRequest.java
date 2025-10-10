package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerRequest {
    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("PrimaryEmailAddr")
    private PrimaryEmailAddr primaryEmailAddr;

    @JsonProperty("PrimaryPhone")
    private PrimaryPhone primaryPhone;

    @JsonProperty("BillAddr")
    private BillAddr billAddr;

    @JsonProperty("Notes")
    private String notes;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BillAddr {
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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryPhone {
        @JsonProperty("FreeFormNumber")
        private String freeFormNumber;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryEmailAddr {
        @JsonProperty("Address")
        private String address;
    }
}
