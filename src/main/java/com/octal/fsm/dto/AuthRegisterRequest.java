package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.intuit.ipp.data.Gender;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AuthRegisterRequest {

    private Long recordId;
    private String uuid;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;

    private String name;
    private String email;
    private String mobileNumber;
    private String address;
    private String primaryLocation;
    private Double lat;
    private Double lng;
    private Gender gender;
    private String leadSourceName;
    private String customerTypeName;
    private LocalDateTime joinedDate;
}
