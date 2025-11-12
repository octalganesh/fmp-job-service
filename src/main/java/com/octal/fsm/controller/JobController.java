package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.JobService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/jobs")
public class JobController extends BaseController {

    private static final Logger logger = LogManager.getLogger(JobController.class);

    @Autowired
    private JobService jobService;

    /**
     * Create a new job
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createJob(@Valid @RequestBody JobDTO.Add addJobDTO, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId=getTenantId(request);
            boolean isSuperAdmin=isSuperAdmin(request);
            String jobId = jobService.addJob(addJobDTO,tenantId,isSuperAdmin);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job created successfully", jobId, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> jobList(@RequestParam(defaultValue = "0") int page,
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
            boolean isSuperAdmin=isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            //Todo List Method to get all Job List.
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job list successfully", jobService.getAllJobs(page, size, sortBy, order, jobType, jobStatus, jobTag, serviceLocationLat, serviceLocationLng, customerType, fromStartDate, toStartDate,location,userName,tenantId,isSuperAdmin,frontOfficeId), "200", HttpStatus.OK), HttpStatus.OK);
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
            boolean isSuperAdmin=isSuperAdmin(request);
            //Todo List Method to get all Job List.
            JobDTO.Detail job = jobService.getJobById(id,userName,tenantId,isSuperAdmin);
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
            boolean isSuperAdmin=isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.assignJobToTechnician(assignJobToTechnician,tenantId,isSuperAdmin, userName);
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
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job list successfully", jobService.jobLeaveReassignHistory(page, size, sortBy, order, fromStartDate, toStartDate,jobId, userName, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
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
    public ResponseEntity<ApiResponse>updateJobTaskStatus(@PathVariable("technicianId") String technicianId,
                                                    @PathVariable("taskId") String taskId,
                                                    @RequestParam("status") String status,
                                                    @RequestParam(value = "note",required = false,defaultValue = "") String note,
                                                    @RequestParam(value = "signature",required = false,defaultValue = "") String signature,
                                                    HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.updateJobTaskStatus(technicianId, taskId, status,note, signature,userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job task status updated successfully", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }
    @PutMapping("/update-job-task/{technicianId}/{taskId}")
    public ResponseEntity<ApiResponse>updateJobTask(@PathVariable("technicianId") String technicianId,
                                                          @PathVariable("taskId") String taskId,
                                                          @RequestParam(value = "note",defaultValue = "") String note,
                                                          HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.updateJobTask(technicianId, taskId,note,userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job task updated successfully", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/add-drawing-to-job-task/{technicianId}/{taskId}")
    public ResponseEntity<ApiResponse>addDrawingToJobTask(@PathVariable("technicianId") String technicianId,
                                                  @PathVariable("taskId") String taskId,
                                                   @RequestBody JobDTO.TaskDrawingRequest taskDrawingRequest,
                                                  HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.addDrawingToJobTask(technicianId, taskId, taskDrawingRequest,userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Drawing added to job task successfully", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error adding drawing to job task: {}", e.getMessage(), e);
            return handleException(e);
        }
    }


//
//    /**
//     * Delete a job
//     */
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse> deleteJob(@PathVariable String id,
//                                                HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//
//            Boolean result = jobService.deleteJob(id);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job deleted successfully", result, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error deleting job: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }
//
//    /**
//     * Get all jobs with pagination
//     */
//    @GetMapping(value = "/list")
//    public ResponseEntity<ApiResponse> getAllJobs(@RequestParam(defaultValue = "0") int page,
//                                                @RequestParam(defaultValue = "10") int size,
//                                                @RequestParam(defaultValue = "createdAt") String sortBy,
//                                                @RequestParam(defaultValue = "desc") String sortDir,
//                                                HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//
//            PageRequest.List listRequest = new PageRequest.List();
//            listRequest.setPageNumber(page);
//            listRequest.setPageSize(size);
//            listRequest.setShortingField(sortBy);
//            listRequest.setAsc("asc".equalsIgnoreCase(sortDir));
//
//            PageItem<JobDTO.List> jobs = jobService.getAllJobs(listRequest);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, CommonConstants.DETAILS_FETCHED, jobs, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error retrieving jobs: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }
//
//    /**
//     * Search jobs
//     */
//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse> searchJobs(@RequestParam String searchTerm,
//                                                 @RequestParam(defaultValue = "0") int page,
//                                                 @RequestParam(defaultValue = "10") int size,
//                                                 @RequestParam(defaultValue = "createdAt") String sortBy,
//                                                 @RequestParam(defaultValue = "desc") String sortDir,
//                                                 HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//
//            PageRequest.List listRequest = new PageRequest.List();
//            listRequest.setPageNumber(page);
//            listRequest.setPageSize(size);
//            listRequest.setShortingField(sortBy);
//            listRequest.setAsc("asc".equalsIgnoreCase(sortDir));
//
//            PageItem<JobDTO.List> jobs = jobService.searchJobs(searchTerm, listRequest);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Jobs search completed successfully", jobs, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error searching jobs: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }
//
//    /**
//     * Get jobs by status
//     */
//    @GetMapping("/status/{status}")
//    public ResponseEntity<ApiResponse> getJobsByStatus(@PathVariable String status,
//                                                      @RequestParam(defaultValue = "0") int page,
//                                                      @RequestParam(defaultValue = "10") int size,
//                                                      @RequestParam(defaultValue = "createdAt") String sortBy,
//                                                      @RequestParam(defaultValue = "desc") String sortDir,
//                                                      HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//            PageRequest.List listRequest = new PageRequest.List();
//            listRequest.setPageNumber(page);
//            listRequest.setPageSize(size);
//            listRequest.setShortingField(sortBy);
//            listRequest.setAsc("asc".equalsIgnoreCase(sortDir));
//
//            PageItem<JobDTO.List> jobs = jobService.getJobsByStatus(status, listRequest);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Jobs retrieved by status successfully", jobs, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error retrieving jobs by status: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }
//
//    /**
//     * Get jobs by priority
//     */
//    @GetMapping("/priority/{priority}")
//    public ResponseEntity<ApiResponse> getJobsByPriority(@PathVariable String priority,
//                                                        @RequestParam(defaultValue = "0") int page,
//                                                        @RequestParam(defaultValue = "10") int size,
//                                                        @RequestParam(defaultValue = "createdAt") String sortBy,
//                                                        @RequestParam(defaultValue = "desc") String sortDir,
//                                                        HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//            PageRequest.List listRequest = new PageRequest.List();
//            listRequest.setPageNumber(page);
//            listRequest.setPageSize(size);
//            listRequest.setShortingField(sortBy);
//            listRequest.setAsc("asc".equalsIgnoreCase(sortDir));
//
//            PageItem<JobDTO.List> jobs = jobService.getJobsByPriority(priority, listRequest);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Jobs retrieved by priority successfully", jobs, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error retrieving jobs by priority: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }
//
//    /**
//     * Change job status
//     */
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<ApiResponse> changeJobStatus(@PathVariable String id,
//                                                      @RequestParam String status,
//                                                      HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//
//            Boolean result = jobService.changeJobStatus(id, status);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job status updated successfully", result, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error changing job status: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }
//
//    /**
//     * Assign technician to job
//     */
//    @PatchMapping("/{jobId}/assign-technician")
//    public ResponseEntity<ApiResponse> assignTechnician(@PathVariable String jobId,
//                                                       @RequestParam String technicianId,
//                                                       HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//
//            JobDTO.Detail updatedJob = jobService.assignTechnician(jobId, technicianId);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Technician assigned successfully", updatedJob, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error assigning technician: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }
//
//    /**
//     * Update job progress
//     */
//    @PatchMapping("/{jobId}/progress")
//    public ResponseEntity<ApiResponse> updateJobProgress(@PathVariable String jobId,
//                                                        @RequestParam String summary,
//                                                        @RequestParam String status,
//                                                        HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//
//            JobDTO.Detail updatedJob = jobService.updateJobProgress(jobId, summary, status);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job progress updated successfully", updatedJob, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error updating job progress: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }
//
//    /**
//     * Complete job
//     */
//    @PatchMapping("/{jobId}/complete")
//    public ResponseEntity<ApiResponse> completeJob(@PathVariable String jobId,
//                                                  @RequestParam String summary,
//                                                  HttpServletRequest request) {
//        try {
//            String userName = request.getHeader(CommonConstants.USER_NAME);
//
//            JobDTO.Detail completedJob = jobService.completeJob(jobId, summary);
//
//            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job completed successfully", completedJob, "200", HttpStatus.OK), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error completing job: {}", e.getMessage(), e);
//            return handleException(e);
//        }
//    }

    @PostMapping("/upfront-invoice")
    public ResponseEntity<ApiResponse> generateUpFrontInvoice(@RequestBody JobDTO.CreateUpFrontInvoiceRequest createUpFrontInvoiceRequest, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin=isSuperAdmin(request);
            jobService.createUpFrontInvoice(createUpFrontInvoiceRequest,tenantId,isSuperAdmin);
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
            boolean isSuperAdmin=isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.updateJobTags(jobId, updateJobTags, userName,tenantId,isSuperAdmin);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Tags Updated Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job tags: {}", e.getMessage(), e);
            return handleException(e);
        }
    }
}
