package com.octal.fsm.service;

import com.octal.fsm.dto.JobCallDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;

import java.util.List;

public interface JobCallHistoryService {

    void saveJobCallHistory(JobCallDTO.Add addJobDTO) throws CodeException;

    PageItem<JobCallDTO.ListResponse> getJobCallHistoriesByJobId(String jobId, int page, int size, String sortBy, Boolean order);

    List<JobCallDTO.ListResponse> getAllJobCallHistoriesByJobId(String jobId) throws CodeException;
}
