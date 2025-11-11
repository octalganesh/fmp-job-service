package com.octal.fsm.dto;

import com.octal.fsm.entities.MultiUserDeviceDetails;
import com.octal.fsm.entities.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TechnicianDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetDetails {
        private String id;
        private String name;
        private String email;
        private String mobileNumber;
        private String employeeId;
        private String profilePicture;
        private String address;
        private String createdAt;
        private String updatedAt;
        private Boolean isActive;
        private Integer assignedLeads;
        private Integer completedJobs;
        private Integer rating;
        private Gender gender;
        private String joinedDate;
        private MultiUserDeviceDetails multiUserDeviceDetails;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TechnicianData {
        private String id;
        private String name;
        private String email;
        private String mobileNumber;
        private String employeeId;
        private String profilePicture;
        private String address;
        private String createdAt;
        private String updatedAt;
        private Boolean isActive;
        private Integer assignedLeads;
        private Integer completedJobs;
        private Integer rating;
        private Gender gender;
        private String joinedDate;
        private MultiUserDeviceDetails multiUserDeviceDetails;
    }


}
