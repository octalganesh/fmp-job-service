package com.octal.fsm.service.impl;

import com.octal.fsm.dto.JobReportSummaryDTO;
import com.octal.fsm.entities.*;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.repositories.*;
import com.octal.fsm.service.JobReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;

@Service
public class JobReportServiceImpl implements JobReportService {

    @Autowired
    private JobReportNativeRepository jobReportNativeRepository;


    @Override
    public JobReportSummaryDTO.Detail getJobReportSummary(JobReportSummaryDTO.Search data,Long tenantId) throws CodeException {

        if (data.getStartDate() == null || data.getEndDate() == null) {
            throw new CodeException("Start date and end date are required", ErrorCode.COMMON);
        }

        JobReportSummaryDTO.Detail dto = new JobReportSummaryDTO.Detail();
        if (data.getStartDate() != null && data.getEndDate() != null) {
            Map<String, Object> result = jobReportNativeRepository.getFullReport(
                    data.getStartDate().atStartOfDay(), data.getEndDate().atTime(23, 59, 59),tenantId);

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

    //For safety if data null then return empty map
    private Map<String, Long> parseJsonMap(ObjectMapper mapper, Object jsonData) {
        if (jsonData == null) return new HashMap<>();
        try {
            return mapper.readValue(jsonData.toString(), new TypeReference<Map<String, Long>>() {});
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
