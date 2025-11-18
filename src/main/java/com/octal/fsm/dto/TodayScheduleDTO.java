package com.octal.fsm.dto;

import lombok.Data;

import javax.persistence.Column;
import java.time.LocalDate;

@Data
public class TodayScheduleDTO {

    private String technicianName;
    private String serviceLocation;
    private LocalDate startDate;
    private LocalDate endDate;
    private String time;
}
