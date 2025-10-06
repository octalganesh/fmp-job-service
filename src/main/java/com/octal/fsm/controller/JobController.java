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
    public ResponseEntity<ApiResponse> createJob(@Valid @RequestBody JobDTO.Add addJobDTO,
                                               HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            String jobId = jobService.addJob(addJobDTO);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job created successfully", jobId, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Update an existing job
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateJob(@PathVariable String id,
                                               @Valid @RequestBody JobDTO.Update updateJobDTO,
                                               HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);

            updateJobDTO.setId(id);
            JobDTO.Detail updatedJob = jobService.updateJob(updateJobDTO);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job updated successfully", updatedJob, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Get job by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getJobById(@PathVariable String id) {
        try {
            JobDTO.Detail job = jobService.getJobById(id);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job retrieved successfully", job, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Delete a job
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteJob(@PathVariable String id,
                                                HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);


            Boolean result = jobService.deleteJob(id);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job deleted successfully", result, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Get all jobs with pagination
     */
    @GetMapping(value = "/list")
    public ResponseEntity<ApiResponse> getAllJobs(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "createdAt") String sortBy,
                                                @RequestParam(defaultValue = "desc") String sortDir,
                                                HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);


            PageRequest.List listRequest = new PageRequest.List();
            listRequest.setPageNumber(page);
            listRequest.setPageSize(size);
            listRequest.setShortingField(sortBy);
            listRequest.setAsc("asc".equalsIgnoreCase(sortDir));

            PageItem<JobDTO.List> jobs = jobService.getAllJobs(listRequest);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, CommonConstants.DETAILS_FETCHED, jobs, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving jobs: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Search jobs
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchJobs(@RequestParam String searchTerm,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "createdAt") String sortBy,
                                                 @RequestParam(defaultValue = "desc") String sortDir,
                                                 HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);


            PageRequest.List listRequest = new PageRequest.List();
            listRequest.setPageNumber(page);
            listRequest.setPageSize(size);
            listRequest.setShortingField(sortBy);
            listRequest.setAsc("asc".equalsIgnoreCase(sortDir));

            PageItem<JobDTO.List> jobs = jobService.searchJobs(searchTerm, listRequest);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Jobs search completed successfully", jobs, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error searching jobs: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Get jobs by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getJobsByStatus(@PathVariable String status,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "createdAt") String sortBy,
                                                      @RequestParam(defaultValue = "desc") String sortDir,
                                                      HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);

            PageRequest.List listRequest = new PageRequest.List();
            listRequest.setPageNumber(page);
            listRequest.setPageSize(size);
            listRequest.setShortingField(sortBy);
            listRequest.setAsc("asc".equalsIgnoreCase(sortDir));

            PageItem<JobDTO.List> jobs = jobService.getJobsByStatus(status, listRequest);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Jobs retrieved by status successfully", jobs, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving jobs by status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Get jobs by priority
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse> getJobsByPriority(@PathVariable String priority,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "createdAt") String sortBy,
                                                        @RequestParam(defaultValue = "desc") String sortDir,
                                                        HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);

            PageRequest.List listRequest = new PageRequest.List();
            listRequest.setPageNumber(page);
            listRequest.setPageSize(size);
            listRequest.setShortingField(sortBy);
            listRequest.setAsc("asc".equalsIgnoreCase(sortDir));

            PageItem<JobDTO.List> jobs = jobService.getJobsByPriority(priority, listRequest);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Jobs retrieved by priority successfully", jobs, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving jobs by priority: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Change job status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> changeJobStatus(@PathVariable String id,
                                                      @RequestParam String status,
                                                      HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);


            Boolean result = jobService.changeJobStatus(id, status);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job status updated successfully", result, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error changing job status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Assign technician to job
     */
    @PatchMapping("/{jobId}/assign-technician")
    public ResponseEntity<ApiResponse> assignTechnician(@PathVariable String jobId,
                                                       @RequestParam String technicianId,
                                                       HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);


            JobDTO.Detail updatedJob = jobService.assignTechnician(jobId, technicianId);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Technician assigned successfully", updatedJob, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error assigning technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Update job progress
     */
    @PatchMapping("/{jobId}/progress")
    public ResponseEntity<ApiResponse> updateJobProgress(@PathVariable String jobId,
                                                        @RequestParam String summary,
                                                        @RequestParam String status,
                                                        HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);


            JobDTO.Detail updatedJob = jobService.updateJobProgress(jobId, summary, status);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job progress updated successfully", updatedJob, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job progress: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Complete job
     */
    @PatchMapping("/{jobId}/complete")
    public ResponseEntity<ApiResponse> completeJob(@PathVariable String jobId,
                                                  @RequestParam String summary,
                                                  HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);

            JobDTO.Detail completedJob = jobService.completeJob(jobId, summary);

            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job completed successfully", completedJob, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error completing job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }
}
