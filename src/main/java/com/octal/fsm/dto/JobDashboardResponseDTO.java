package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDashboardResponseDTO {

    @Data
    public static class Detail {
        private Long activeJobs;//
        private Double totalRevenueGenerated;//
        private Double totalPendingAmounts;//
        private List<LocationSummaryDTO> popularServiceLocations;
        private Long totalJobs;
        private Map<String, Long> jobsByType;
        private List<RevenueOverviewPointDTO> revenueOverview;
    }

    @Data
    public static class Search {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationSummaryDTO {
        private String name; // service_location
        private int jobs;
        private double revenue;
    }

    @Data
    @NoArgsConstructor
    public static class RevenueOverviewPointDTO {
        private String month; // "Jan", "Feb" etc.
        private double value; // revenue value
    }


}
