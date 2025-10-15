package com.octal.fsm.service.impl;

import com.octal.fsm.dto.JobCallDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.JobCallHistory;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.repositories.JobCallHistoryRepository;
import com.octal.fsm.repositories.JobRepository;
import com.octal.fsm.service.JobCallHistoryService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class JobCallHistoryServiceImpl implements JobCallHistoryService {

    @Autowired
    private JobCallHistoryRepository jobCallHistoryRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SpecificationFactory<JobCallHistory> jobCallHistorySpecificationFactory;

    @Override
    public void saveJobCallHistory(JobCallDTO.Add addJobDTO) throws CodeException {
        if(TextUtils.isEmpty(addJobDTO.getJobId()))
            throw new CodeException("Job id is required to save job call history", ErrorCode.COMMON);
        Boolean jobCheck = jobRepository.existsByUuidAndDeletedFalse(addJobDTO.getJobId());
        if(!jobCheck)
            throw new CodeException("Job not found", ErrorCode.COMMON);
        JobCallHistory jobCallHistory = new JobCallHistory();
        jobCallHistory.setCallNote(addJobDTO.getCallNote());
        jobCallHistory.setJobId(addJobDTO.getJobId());
        jobCallHistory.setCreatedById(addJobDTO.getCreatedById());
        jobCallHistory.setCreatedByName(addJobDTO.getCreatedByName());
        jobCallHistoryRepository.save(jobCallHistory);
    }

    @Override
    public PageItem<JobCallDTO.ListResponse> getJobCallHistoriesByJobId(String jobId, int page, int size, String sortBy, Boolean order) {
        GenericSpecificationsBuilder<JobCallHistory> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(order)) {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).descending());
        }
        builder.with(jobCallHistorySpecificationFactory.isEqual("deleted", false));
        builder.with(jobCallHistorySpecificationFactory.isEqual("jobId", jobId));
        Page<JobCallHistory> pagedResult = jobCallHistoryRepository.findAll(builder.build(), pageable);
        List<JobCallDTO.ListResponse> responseList = new ArrayList<>();
        for (JobCallHistory job : pagedResult.getContent()) {
            JobCallDTO.ListResponse response = new JobCallDTO.ListResponse();
            response.setId(job.getUuid());
            response.setCallNote(job.getCallNote());
            response.setCreatedAt(job.getCreatedAt());
            response.setCreatedByName(job.getCreatedByName());
            response.setCreatedById(job.getCreatedById());
            responseList.add(response);
        }

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, page,
                size);
    }
}
