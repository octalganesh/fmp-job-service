package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.JobTypeService;
import com.octal.fsm.utils.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/job-type")
public class JobTypeController extends BaseController {

    private static final Logger logger = LogManager.getLogger(JobTypeController.class);


    @Autowired
    private JobTypeService jobTypeService;


    @PostMapping(value = "/add-jobType")
    public ResponseEntity<ApiResponse>addJobType(@RequestBody JobTypeDTO.Add jobTypeDTO, HttpServletRequest request){
        logger.info("JobTypeController.addJobType");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            String messageResponse = TextUtils.isEmpty(jobTypeDTO.getId()) ? "jobType added Successfully!" : "jobType updated Successfully!";
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, messageResponse,jobTypeService.addJobType(jobTypeDTO), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> JobTypeList(@Valid @RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        logger.info("JobTypeController./list");
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, CommonConstants.DETAILS_FETCHED, jobTypeService.getAllJobTypes(listRequest), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }


    @DeleteMapping("/delete/by/id/{id}")
    public ResponseEntity<ApiResponse> deleteId(@PathVariable("id") String id, HttpServletRequest request) {
        logger.info("JobTypeController./delete/by/id");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "JobType deleted successfully", jobTypeService.deleteById(id), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<ApiResponse> getJobTypeById(@PathVariable("id") String id, HttpServletRequest request) {
        logger.info("JobTypeController./by/id");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "fetched successfully!", jobTypeService.getJobTypeByUuid(id), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/change/status/{id}")
    public ResponseEntity<ApiResponse> changeStatus(HttpServletRequest request, @PathVariable("id") String id) {
        logger.info("JobTypeController./change/status");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            Boolean status = jobTypeService.changeStatus(id);
            String messageResponse = Boolean.TRUE.equals(status) ? "JobType activated Successfully!" : "JobType deactivated Successfully!";
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, messageResponse, null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }

    }

    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse> getAllJobs(HttpServletRequest request) {
        logger.info("JobTypeController./get-all");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, CommonConstants.DETAILS_FETCHED, jobTypeService.getAllJobs(), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }



}
