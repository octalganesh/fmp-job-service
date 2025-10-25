package com.octal.fsm.service;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;

public interface JobService {

    String addJob(JobDTO.Add addJobDTO) throws CodeException;

    void createUpFrontInvoice(JobDTO.CreateUpFrontInvoiceRequest createUpFrontInvoice) throws CodeException;

    PageItem<JobDTO.InvoiceListResponse> getAllJobInvoices(int page, int size, String sortBy, Boolean order, String jobId, String loggedInUserEmail) throws CodeException;

    void updateJobTags(String jobId, JobDTO.UpdateJobTags updateJobTags, String loggedInUserEmail) throws CodeException;

    PageItem<JobDTO.JobListResponse> getAllJobs(int page, int size, String sortBy, Boolean order, String jobType, String jobStatus, String jobTag, Double serviceLocationLat, Double serviceLocationLng, String customerType, String fromStartDate, String toStartDate, String loggedInUserEmail) throws CodeException;

    //    JobDTO.Detail updateJob(JobDTO.Update updateJobDTO) throws CodeException;
//
//    Boolean deleteJob(String id) throws CodeException;
//
    JobDTO.Detail getJobById(String id, String loggedInUserEmail) throws CodeException;

    PageItem<JobDTO.JobTaskListResponse> getJobTask(int page, int size, String sortBy, Boolean order, String jobId, String loggedInUserEmail) throws CodeException;

    void assignJobToTechnician(JobDTO.AssignJobToTechnician assignJobToTechnician, String loggedInUserEmail) throws CodeException;


    PageItem<JobDTO.DetailsForTechnician> getJobTasksForTechnician(JobDTO.JobFilterRequest filterRequest, String technicianId, String loggedInUserEmail) throws CodeException;


    JobDTO.DetailsForTechnician getJobTaskDetailsForTechnician(String technicianId, String taskId, String userName) throws CodeException;

    void updateJobTaskStatus(String technicianId, String taskId, String status, String note,String signature, String userName) throws CodeException;

    void updateAssignedTaskWithDocumentType(String jobTaskMappingId, JobDTO.UpdateAssignedTaskWithDocumentType updateAssignedTaskWithDocumentType, String loggedInUserEmail) throws CodeException;
//    Boolean changeJobStatus(String id, String status) throws CodeException;
//
//    PageItem<JobDTO.List> getAllJobs(PageRequest.List listRequest);
//
//    PageItem<JobDTO.List> searchJobs(String searchTerm, PageRequest.List listRequest);
//
//    PageItem<JobDTO.List> getJobsByStatus(String status, PageRequest.List listRequest);
//
//    PageItem<JobDTO.List> getJobsByPriority(String priority, PageRequest.List listRequest);
//
//    JobDTO.Detail assignTechnician(String jobId, String technicianId) throws CodeException;
//
//    JobDTO.Detail updateJobProgress(String jobId, String summary, String status) throws CodeException;
//
//    JobDTO.Detail completeJob(String jobId, String summary) throws CodeException;
}
