package com.octal.fsm.dto;


import com.octal.fsm.entities.enums.Gender;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class FrontOfficeStaffDTO {


    @Data
    public static class list {
        private String id;
        private String name;
        private String email;
        private String mobileNumber;
        private String employeeId;
        private String createdAt;
        private String updatedAt;
        private Boolean isActive;
        private String designation;
        private Gender gender;
        private String joinedDate;
        private MultiUserDeviceDetailsDTO multiUserDeviceDetails;

    }
}
