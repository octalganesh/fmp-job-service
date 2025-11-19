package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class TechnicianJobSummaryDTO {

    private String technicianId;
    private String name;
    private String email;
    private String mobileNumber;
    private String profilePicture;

    private boolean available;
    private Long totalJobsCompleted;
    private String joinedDate;

}
