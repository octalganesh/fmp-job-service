package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobCallDTO;
import com.octal.fsm.service.JobCallHistoryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/jobs/call")
public class JobCallHistoryController extends BaseController {

    private static final Logger logger = LogManager.getLogger(JobCallHistoryController.class);

    @Autowired
    private JobCallHistoryService jobCallHistoryService;

    @PostMapping
    public ResponseEntity<ApiResponse> createCallLog(@Valid @RequestBody JobCallDTO.Add addJobDTO, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            jobCallHistoryService.saveJobCallHistory(addJobDTO);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Call Log Successfully", null, "200", HttpStatus.OK), HttpStatus.OK);
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
                                               @RequestParam("jobId") String jobId,
                                               HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            //Todo List Method to get all Job List.
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job Call Log List successfully", jobCallHistoryService.getJobCallHistoriesByJobId(jobId, page, size, sortBy, order), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }
}
