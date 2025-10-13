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
            String jobId = jobService.addJob(addJobDTO);
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
                                               HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            //Todo List Method to get all Job List.
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job list successfully", jobService.getAllJobs(page, size, sortBy, order, jobType, jobStatus, jobTag, serviceLocationLat, serviceLocationLng, customerType, fromStartDate, toStartDate, userName), "200", HttpStatus.OK), HttpStatus.OK);
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
            //Todo List Method to get all Job List.
            JobDTO.Detail job = jobService.getJobById(id, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job retrieved successfully", job, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/assign-technician-job-task")
    public ResponseEntity<ApiResponse> getJobTask(@RequestBody JobDTO.AssignJobToTechnician assignJobToTechnician, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.assignJobToTechnician(assignJobToTechnician, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Task Assigned.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job: {}", e.getMessage(), e);
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

    @PostMapping("/upfront-invoice")
    public ResponseEntity<ApiResponse> generateUpFrontInvoice(@RequestBody JobDTO.CreateUpFrontInvoiceRequest createUpFrontInvoiceRequest, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.createUpFrontInvoice(createUpFrontInvoiceRequest);
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
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobService.updateJobTags(jobId, updateJobTags, userName);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Tags Updated Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job tags: {}", e.getMessage(), e);
            return handleException(e);
        }
    }
}
