package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class QuickBookDTO {


    @Data
    public static class CreateCustomer {
        private String id;
        private String name;
        private String email;
        private String mobileNumber;
        private String primaryLocation;
        private String address;
    }
}
