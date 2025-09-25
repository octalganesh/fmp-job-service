package com.octal.fsm.service;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;

public interface JobService {

    String addJob(JobDTO.Add addJobDTO) throws CodeException;

    JobDTO.Detail updateJob(JobDTO.Update updateJobDTO) throws CodeException;

    Boolean deleteJob(String id) throws CodeException;

    JobDTO.Detail getJobById(String id) throws CodeException;

    Boolean changeJobStatus(String id, String status) throws CodeException;

    PageItem<JobDTO.List> getAllJobs(PageRequest.List listRequest);

    PageItem<JobDTO.List> searchJobs(String searchTerm, PageRequest.List listRequest);

    PageItem<JobDTO.List> getJobsByStatus(String status, PageRequest.List listRequest);

    PageItem<JobDTO.List> getJobsByPriority(String priority, PageRequest.List listRequest);

    JobDTO.Detail assignTechnician(String jobId, String technicianId) throws CodeException;

    JobDTO.Detail updateJobProgress(String jobId, String summary, String status) throws CodeException;

    JobDTO.Detail completeJob(String jobId, String summary) throws CodeException;
}
