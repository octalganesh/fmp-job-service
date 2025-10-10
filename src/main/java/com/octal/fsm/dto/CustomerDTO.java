package com.octal.fsm.dto;

import com.octal.fsm.entities.enums.Gender;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class CustomerDTO {


    @Data
    public static class AddCustomer{
        private String id;
        private String name;
        private String email;
        private String mobileNumber;
        private String address;
        //private String registeredDate;
        private String primaryLocation;
        private Double lat;
        private Double lng;
        private Gender gender;
        @NotNull(message = "Customer type is required")
        private String customerTypeId;
        @NotNull(message = "Lead source is required")
        private String leadSourceId;
        private boolean isActive;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate joinedDate;
    }
}
