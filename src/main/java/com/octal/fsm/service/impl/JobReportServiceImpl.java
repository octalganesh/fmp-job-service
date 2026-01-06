package com.octal.fsm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octal.fsm.dto.JobDashboardResponseDTO;
import com.octal.fsm.dto.JobReportSummaryDTO;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.repositories.JobReportNativeRepository;
import com.octal.fsm.repositories.JobRepository;
import com.octal.fsm.service.JobReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobReportServiceImpl implements JobReportService {

    @Autowired
    private JobReportNativeRepository jobReportNativeRepository;

    @Autowired
    private JobRepository jobRepository;

    @Override
    public JobReportSummaryDTO.Detail getJobReportSummary(JobReportSummaryDTO.Search data, Long tenantId) throws CodeException {

        if (data.getStartDate() == null || data.getEndDate() == null) {
            throw new CodeException("Start date and end date are required", ErrorCode.COMMON);
        }

        JobReportSummaryDTO.Detail dto = new JobReportSummaryDTO.Detail();
        if (data.getStartDate() != null && data.getEndDate() != null) {
            Map<String, Object> result = jobReportNativeRepository.getFullReport(
                    data.getStartDate().atStartOfDay(), data.getEndDate().atTime(23, 59, 59), tenantId);

            dto.setTotalNumberOfJobs(getLong(result.get("total_jobs")));
            dto.setActiveJobs(getLong(result.get("active_jobs")));
            dto.setTotalRevenueCollected(getDouble(result.get("total_revenue_collected")));
            dto.setTotalRevenueGenerated(getDouble(result.get("total_revenue_generated")));
            dto.setTotalPendingAmounts(getDouble(result.get("total_pending_amounts")));
            dto.setTotalInvoicesGenerated(getLong(result.get("total_invoices")));
            dto.setTotalAppointmentsScheduled(getLong(result.get("total_appointments")));

            ObjectMapper mapper = new ObjectMapper();
            dto.setTotalJobsPerJobType(parseJsonMap(mapper, result.get("jobs_per_job_type")));
            dto.setPopularJobTypes(parseJsonMap(mapper, result.get("popular_job_types")));
            dto.setTotalJobsPerServiceLocation(parseJsonMap(mapper, result.get("jobs_per_service_location")));
            dto.setPopularServiceLocations(parseJsonMap(mapper, result.get("popular_service_locations")));

        }
        return dto;
    }

    @Override
    public JobDashboardResponseDTO.Detail getDashboardData(JobDashboardResponseDTO.Search search, Long tenantId) throws CodeException {
        if (search.getStartDate() == null || search.getEndDate() == null) {
//            throw new CodeException("Start date and end date are required", ErrorCode.COMMON);
            normalizeSearchDates(search, tenantId);
        }

        JobDashboardResponseDTO.Detail dto = new JobDashboardResponseDTO.Detail();
        if (search.getStartDate() != null && search.getEndDate() != null) {
            Map<String, Object> result = jobReportNativeRepository.getJobDashboardReport(
                    search.getStartDate().atStartOfDay(), search.getEndDate().atTime(23, 59, 59), tenantId);

            dto.setActiveJobs(getLong(result.get("active_jobs")));
            dto.setTotalJobs(getLong(result.get("total_jobs")));
            dto.setTotalRevenueGenerated(getDouble(result.get("total_revenue_generated")));
            dto.setTotalPendingAmounts(getDouble(result.get("total_pending_payment")));
            ObjectMapper mapper = new ObjectMapper();
            dto.setPopularServiceLocations(parsePopularServiceLocations(mapper, result.get("popular_service_locations")));
            dto.setJobsByType(parseJsonMap(mapper, result.get("job_type_counts")));

        }
        return dto;
    }

    private void normalizeSearchDates(JobDashboardResponseDTO.Search search, Long tenantId) {
        if (search.getStartDate() == null && search.getEndDate() == null) {
            LocalDateTime firstCreatedDate = jobRepository.findEarliestJobCreatedAtByTenantId(tenantId);
            if (firstCreatedDate == null) {
                firstCreatedDate = LocalDateTime.now();
            }
            search.setStartDate(firstCreatedDate.toLocalDate());
            search.setEndDate(LocalDate.now());
        }
    }

    private List<JobDashboardResponseDTO.LocationSummaryDTO> parsePopularServiceLocations(ObjectMapper mapper, Object rawJson) {
        if (rawJson == null) return List.of();

        try {
            // Parse the JSON into Map<String, Map<String, Object>>
            Map<String, Map<String, Object>> data = mapper.readValue(
                    rawJson.toString(), new TypeReference<>() {
                    }
            );

            // Convert Map → List<LocationSummaryDTO>
            return data.entrySet().stream()
                    .map(e -> new JobDashboardResponseDTO.LocationSummaryDTO(
                            e.getKey(),
                            ((Number) e.getValue().getOrDefault("jobs", 0)).intValue(),
                            ((Number) e.getValue().getOrDefault("revenue", 0.0)).doubleValue()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            return List.of();
        }
    }


    //For safety if data null then return empty map
    private Map<String, Long> parseJsonMap(ObjectMapper mapper, Object jsonData) {
        if (jsonData == null) return new HashMap<>();
        try {
            return mapper.readValue(jsonData.toString(), new TypeReference<Map<String, Long>>() {
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private Long getLong(Object value) {
        return value != null ? ((Number) value).longValue() : 0L;
    }

    private Double getDouble(Object value) {
        return value != null ? ((Number) value).doubleValue() : 0.0;
    }
}
