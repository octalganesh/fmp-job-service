package com.octal.fsm.service;

import com.octal.fsm.dto.JobReportSummaryDTO;
import com.octal.fsm.exceptions.CodeException;

public interface JobReportService {

    JobReportSummaryDTO.Detail getJobReportSummary(JobReportSummaryDTO.Search search,Long tenantId) throws CodeException;
}
