package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.*;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.JobReportService;
import com.octal.fsm.service.JobService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController extends BaseController {

    private static final Logger logger = LogManager.getLogger(JobController.class);

    @Autowired
    private JobService jobService;
    @Autowired
    private JobReportService jobReportService;

    /**
     * Create a new job
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createJob(@Valid @RequestBody JobDTO.Add addJobDTO, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String jobId = jobService.addJob(addJobDTO, tenantId, isSuperAdmin);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job created successfully", jobId, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> jobList(@RequestParam(value = "searchText", defaultValue = "") String searchText,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "createdAt") String sortBy,
                                               @RequestParam(defaultValue = "true") Boolean order,
                                               @RequestParam(defaultValue = "") String jobType,
                                               @RequestParam(defaultValue = "") String jobStatus,
                                               @RequestParam(defaultValue = "") String jobTag,
                                               @RequestParam(defaultValue = "") Double serviceLocationLat,
                                               @RequestParam(defaultValue = "") Double serviceLocationLng,
                                               @RequestParam(defaultValue = "") String customerType,
                                               @RequestParam(defaultValue = "") String fromStartDate,
                                               @RequestParam(defaultValue = "") String toStartDate,
                                               @RequestParam(defaultValue = "") String location,
                                               @RequestParam(defaultValue = "") String frontOfficeId,
                                               HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            //Todo List Method to get all Job List.
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job list successfully", jobService.getAllJobs(searchText,page, size, sortBy, order, jobType, jobStatus, jobTag, serviceLocationLat, serviceLocationLng, customerType, fromStartDate, toStartDate, location, userName, tenantId, isSuperAdmin, frontOfficeId), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Get job by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getJobById(@PathVariable String id, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            //Todo List Method to get all Job List.
            JobDTO.Detail job = jobService.getJobById(id, userName, tenantId, isSuperAdmin);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job retrieved successfully", job, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/assign-technician-job-task")
    public ResponseEntity<ApiResponse> getJobTask(@RequestBody JobDTO.AssignJobToTechnician assignJobToTechnician, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.assignJobToTechnician(assignJobToTechnician, tenantId, isSuperAdmin, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Task Assigned.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }


    @PostMapping("/leave-assign")
    public ResponseEntity<ApiResponse> leaveOrReAssignJob(@Valid @RequestBody JobDTO.LeaveJob leaveJob, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.leaveOrReAssignJob(leaveJob, tenantId, isSuperAdmin, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job removed Assigned.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/job-leave-assign-history")
    public ResponseEntity<ApiResponse> jobLeaveReassignHistory(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "createdAt") String sortBy,
                                                               @RequestParam(defaultValue = "true") Boolean order,
                                                               @RequestParam String jobId,
                                                               @RequestParam(defaultValue = "") String fromStartDate,
                                                               @RequestParam(defaultValue = "") String toStartDate,
                                                               HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            //Todo List Method to get all Job List.
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job list successfully", jobService.jobLeaveReassignHistory(page, size, sortBy, order, fromStartDate, toStartDate, jobId, userName, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PutMapping("/update-assigned-task-with-document-type/{jobTaskMappingId}")
    public ResponseEntity<ApiResponse> updateAssignedTaskWithDocumentType(@PathVariable("jobTaskMappingId") String jobTaskMappingId, @RequestBody JobDTO.UpdateAssignedTaskWithDocumentType updateAssignedTaskWithDocumentType, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.updateAssignedTaskWithDocumentType(jobTaskMappingId, updateAssignedTaskWithDocumentType, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Task Update Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/job-task/{jobId}")
    public ResponseEntity<ApiResponse> getJobTask(@PathVariable String jobId, @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "createdAt") String sortBy,
                                                  @RequestParam(defaultValue = "true") Boolean order, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Task List.", jobService.getJobTask(page, size, sortBy, order, jobId, userName), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/tasks-for-technician/{technicianId}")
    public ResponseEntity<ApiResponse> getJobTasksForTechnician(@RequestBody JobDTO.JobFilterRequest filterRequest, @PathVariable("technicianId") String technicianId, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            PageItem<JobDTO.DetailsForTechnician> jobTasksList = jobService.getJobTasksForTechnician(filterRequest, technicianId, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job tasks for technician retrieved successfully", jobTasksList, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job tasks for technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/task-for-technician/{technicianId}/{taskId}")
    public ResponseEntity<ApiResponse> getJobTaskDetailsForTechnician(@PathVariable("technicianId") String technicianId, @PathVariable("taskId") String taskId, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            JobDTO.DetailsForTechnician jobTaskDetails = jobService.getJobTaskDetailsForTechnician(technicianId, taskId, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job task details for technician retrieved successfully", jobTaskDetails, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job task details for technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PutMapping("/update-job-task-status/{technicianId}/{taskId}")
    public ResponseEntity<ApiResponse> updateJobTaskStatus(@PathVariable("technicianId") String technicianId,
                                                           @PathVariable("taskId") String taskId,
                                                           @RequestParam("status") String status,
                                                           @RequestParam(value = "note", required = false, defaultValue = "") String note,
                                                           @RequestParam(value = "signature", required = false, defaultValue = "") String signature,
                                                           HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            jobService.updateJobTaskStatus(technicianId, taskId, status, note, signature, userName, tenantId);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job task status updated successfully", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PutMapping("/update-job-task/{technicianId}/{taskId}")
    public ResponseEntity<ApiResponse> updateJobTask(@PathVariable("technicianId") String technicianId,
                                                     @PathVariable("taskId") String taskId,
                                                     @RequestParam(value = "note", defaultValue = "") String note,
                                                     HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.updateJobTask(technicianId, taskId, note, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job task updated successfully", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/add-drawing-to-job-task/{technicianId}/{taskId}")
    public ResponseEntity<ApiResponse> addDrawingToJobTask(@PathVariable("technicianId") String technicianId,
                                                           @PathVariable("taskId") String taskId,
                                                           @RequestBody JobDTO.TaskDrawingRequest taskDrawingRequest,
                                                           HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.addDrawingToJobTask(technicianId, taskId, taskDrawingRequest, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Drawing added to job task successfully", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error adding drawing to job task: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/upfront-invoice")
    public ResponseEntity<ApiResponse> generateUpFrontInvoice(@RequestBody JobDTO.CreateUpFrontInvoiceRequest createUpFrontInvoiceRequest, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            jobService.createUpFrontInvoice(createUpFrontInvoiceRequest, tenantId, isSuperAdmin);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Invoice Generate Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/invoice/list")
    public ResponseEntity<ApiResponse> getInvoiceList(@RequestParam("jobId") String jobId, @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "createdAt") String sortBy,
                                                      @RequestParam(defaultValue = "true") Boolean order, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Invoice List Successfully.", jobService.getAllJobInvoices(page, size, sortBy, order, jobId, userName), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PutMapping("/update-job-tags/{jobId}")
    public ResponseEntity<ApiResponse> updateJobTags(@PathVariable("jobId") String jobId, @RequestBody JobDTO.UpdateJobTags updateJobTags, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.updateJobTags(jobId, updateJobTags, userName, tenantId, isSuperAdmin);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Tags Updated Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job tags: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/getJobReport")
    public ResponseEntity<ApiResponse> getJobReport(@RequestBody JobReportSummaryDTO.Search search, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Report Generated Successfully.", jobReportService.getJobReportSummary(search, tenantId), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while generating report : {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboardData(@RequestBody JobDashboardResponseDTO.Search search, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Dashboard data Generated Successfully.", jobReportService.getDashboardData(search, tenantId), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while generating Dashboard data : {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/get-technician-task-summary")
    public ResponseEntity<ApiResponse> getTechnicianTaskSummary(@RequestBody List<String> technicianUuids, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Technician Task Summary Generated Successfully.", jobService.getTechnicianTaskSummary(technicianUuids, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while generating Technician Task Summary : {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/forms/save-form")
    public ResponseEntity<ApiResponse> addHTMLFormPage(@RequestBody HTMLFormDTO.Add add, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "HTML form added for technician successfully", jobService.saveTechnicianHtmlForm(add, tenantId), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job tasks for technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/forms/by-task/{taskId}")
    public ResponseEntity<ApiResponse> getHTMLFormsDetails(@PathVariable("taskId") String taskId, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Forms Details retrieved successfully", jobService.getFormsWithTaskId(taskId, tenantId), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving Forms Details for technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/add-job-status")
    public ResponseEntity<ApiResponse> addJobStatus(@RequestBody List<JobDTO.AddJobStatus> addJobStatus,
                                                    HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            jobService.addJobStatus(addJobStatus, tenantId, isSuperAdmin, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Status Added.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error adding job status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/job-status/list")
    public ResponseEntity<ApiResponse> getJobStatusList(HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Status List.", jobService.getAllJobStatus(tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job status list: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/process-job-task/{jobId}")
    public ResponseEntity<ApiResponse> updateJobTaskDetails(@PathVariable("jobId") String jobId,
                                                            @RequestBody JobDTO.UpdateJobTaskDetails updateJobTaskDetails,
                                                            HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            jobService.updateJobTaskDetails(jobId, updateJobTaskDetails, tenantId, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Task Details Updated Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task details: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/get-technician-for-front-dashboard")
    public ResponseEntity<ApiResponse> getTechForFrontDashboard(HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Technician Details retrieved successfully", jobService.getTechnicianAssociationNeeded(tenantId,isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving Forms Details for technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/get-current-day-task")
    public ResponseEntity<ApiResponse> getCurrentDaysTask(HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Technician Details retrieved successfully", jobService.getTodayScheduled(tenantId,isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving Forms Details for technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/get-task-list")
    public ResponseEntity<ApiResponse> getJobTaskList(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Task Details Updated Successfully.", jobService.getJobTaskList(listRequest, tenantId,  isSuperAdmin(request)), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task details: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/get-job-completed-invoice")
    public ResponseEntity<ApiResponse> getJobCompletedInvoiceList(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Task Details Updated Successfully.", jobService.getJobCompletedInvoiceList(listRequest, tenantId,  isSuperAdmin(request)), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task details: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/task/manager-list")
    public ResponseEntity<ApiResponse> getTaskManagerList(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Task Details Updated Successfully.", jobService.getTaskManagerList(listRequest, tenantId,  isSuperAdmin(request)), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task details: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/task/by-task-id/{taskId}")
    public ResponseEntity<ApiResponse> getTaskByTaskId(@PathVariable("taskId") String taskId, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Task Details retrieved successfully", jobService.getTaskByTaskId(taskId,tenantId,isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving Forms Details for technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

}
