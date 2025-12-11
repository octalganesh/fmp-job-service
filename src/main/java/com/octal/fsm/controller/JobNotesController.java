package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobCallDTO;
import com.octal.fsm.dto.JobNotesDTO;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.JobNotesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/job-notes")
public class JobNotesController extends BaseController{

    private static final Logger logger = LogManager.getLogger(JobNotesController.class);

    @Autowired
    private JobNotesService jobNotesService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addNotes(@RequestBody JobNotesDTO.Add addNotes, HttpServletRequest request) {
        logger.info("JobNotesController./addNotes");
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            boolean isSuperAdmin = isSuperAdmin(request);
            String message = (addNotes.getId() == null)? "Job Note Added Successfully": "Job Note Updated Successfully";
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, message, jobNotesService.addNotes(addNotes, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error saving notes : {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping(value = "/get/by/{jobId}")
    public ResponseEntity<ApiResponse> getByJobId(@PathVariable("jobId") String jobId, HttpServletRequest request) {
        logger.info("JobNotesController./getByJobId");
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, CommonConstants.DETAILS_FETCHED, jobNotesService.getJobNotesByJobId(jobId), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error getting job notes: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getJobNotesList(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        logger.info("JobNotesController./getJobNotesList");
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin=isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, CommonConstants.DETAILS_FETCHED, jobNotesService.getAllJobNotes(listRequest), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }


}
