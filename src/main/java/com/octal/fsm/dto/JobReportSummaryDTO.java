package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobReportSummaryDTO {

    @Data
    public static class Detail {
        private Double totalRevenueCollected;
        private Long totalNumberOfJobs;
        private Long activeJobs;
        private Map<String, Long> totalJobsPerJobType;
        private Map<String, Long> totalJobsPerServiceLocation;
        private Double totalRevenueGenerated;
        private Double totalPendingAmounts;
        private Map<String, Long> popularServiceLocations;
        private Map<String, Long> popularJobTypes;
        private Long totalAppointmentsScheduled;
        private Long totalInvoicesGenerated;
    }

    @Data
    public static class Search {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;
    }

}
