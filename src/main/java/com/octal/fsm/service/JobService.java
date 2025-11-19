package com.octal.fsm.service;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.*;
import com.octal.fsm.exceptions.CodeException;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

public interface JobService {

    String addJob(JobDTO.Add addJobDTO, Long tenantId, boolean isSuperAdmin) throws CodeException;

    void createUpFrontInvoice(JobDTO.CreateUpFrontInvoiceRequest createUpFrontInvoice, Long tenantId, Boolean isSuperAdmin) throws CodeException;

    PageItem<JobDTO.InvoiceListResponse> getAllJobInvoices(int page, int size, String sortBy, Boolean order, String jobId, String loggedInUserEmail) throws CodeException;

    void updateJobTags(String jobId, JobDTO.UpdateJobTags updateJobTags, String loggedInUserEmail, Long tenantId, Boolean isSuperAdmin) throws CodeException;

    PageItem<JobDTO.JobListResponse> getAllJobs(String txt, int page, int size, String sortBy, Boolean order, String jobType, String jobStatus, String jobTag, Double serviceLocationLat, Double serviceLocationLng, String customerType, String fromStartDate, String toStartDate, String loggedInUserEmail, String location, Long tenantId, Boolean isSuperAdmin, String frontOfficeId) throws CodeException;

    //    JobDTO.Detail updateJob(JobDTO.Update updateJobDTO) throws CodeException;
//
//    Boolean deleteJob(String id) throws CodeException;
//
    JobDTO.Detail getJobById(String id, String loggedInUserEmail, Long tenantId, Boolean isSuperAdmin) throws CodeException;

    PageItem<JobDTO.JobTaskListResponse> getJobTask(int page, int size, String sortBy, Boolean order, String jobId, String loggedInUserEmail) throws CodeException;

    void assignJobToTechnician(JobDTO.AssignJobToTechnician assignJobToTechnician, Long tenantId, Boolean isSuperAdmin, String loggedInUserEmail) throws CodeException;


    PageItem<JobDTO.DetailsForTechnician> getJobTasksForTechnician(JobDTO.JobFilterRequest filterRequest, String technicianId, String loggedInUserEmail) throws CodeException;


    void addJobStatus(List<JobDTO.AddJobStatus> addJobStatus, Long tenantId, boolean isSuperAdmin, String userName);

    JobDTO.DetailsForTechnician getJobTaskDetailsForTechnician(String technicianId, String taskId, String userName) throws CodeException;

    void updateJobTaskStatus(String technicianId, String taskId, String status, String note, String signature, String userName, Long tenantId) throws CodeException;

    void updateAssignedTaskWithDocumentType(String jobTaskMappingId, JobDTO.UpdateAssignedTaskWithDocumentType updateAssignedTaskWithDocumentType, String loggedInUserEmail) throws CodeException;

    void updateJobTask(String technicianId, String taskId, String note, String userName) throws CodeException;

    void addDrawingToJobTask(String technicianId, String taskId, JobDTO.TaskDrawingRequest taskDrawingRequest, String userName) throws CodeException;

    void leaveOrReAssignJob(JobDTO.@Valid LeaveJob leaveJob, Long tenantId, boolean isSuperAdmin, String userName) throws CodeException;

    PageItem<JobDTO.JobHistoryDTO> jobLeaveReassignHistory(int page, int size, String sortBy, Boolean order, String fromStartDate, String toStartDate, String jobId, String userName, Long tenantId, boolean isSuperAdmin);

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

    FormsResponseDTO getFormsWithTaskId(String taskId, Long tenantId) throws CodeException;


    List<JobDTO.JobStatusDetail> getAllJobStatus(Long tenantId, boolean isSuperAdmin);

    ResponseEntity<ApiResponse> getFrontOfficeDevices(String id, Long tenantId) throws CodeException;

    HashMap<String, TechnicianDTO.TaskStats> getTechnicianTaskSummary(List<String> technicianUuids, Long tenantId, boolean isSuperAdmin);

    ResponseEntity<ApiResponse> saveTechnicianHtmlForm(HTMLFormDTO.Add add, Long tenantId) throws CodeException;

    JobTaskMappingWithHTMLFormDTO getTechnicianHtmlForm(String taskId, Long tenantId) throws CodeException;

    void updateJobTaskDetails(String jobId, JobDTO.UpdateJobTaskDetails updateJobTaskDetails, Long tenantId, String userName) throws CodeException;
}
