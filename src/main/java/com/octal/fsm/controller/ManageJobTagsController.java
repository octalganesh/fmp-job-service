package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobTagDTO;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.JobTagService;
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
@RequestMapping("/job-tags")
public class ManageJobTagsController extends BaseController {

    private static final Logger logger = LogManager.getLogger(ManageJobTagsController.class);


    @Autowired
    private JobTagService jobTagService;


    @PostMapping(value = "/add-jobTag")
    public ResponseEntity<ApiResponse> addJobTag(@RequestBody JobTagDTO.Add jobTagAdd, HttpServletRequest request) {
        logger.info("JobTagController.addJobTag");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String messageResponse = TextUtils.isEmpty(jobTagAdd.getId()) ? "JobTag added Successfully!" : "JobTag updated Successfully!";
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, messageResponse, jobTagService.addJobTag(jobTagAdd, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> JobTagList(@Valid @RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        logger.info("JobTagController./list");
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String userName = request.getHeader(CommonConstants.USER_NAME);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, CommonConstants.DETAILS_FETCHED, jobTagService.getAllJobTags(listRequest, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }


    @DeleteMapping("/delete/by/id/{id}")
    public ResponseEntity<ApiResponse> deleteId(@PathVariable("id") String id, HttpServletRequest request) {
        logger.info("JobTagController./delete/by/id");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "JobTag deleted successfully", jobTagService.deleteById(id), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<ApiResponse> getJobTagById(@PathVariable("id") String id, HttpServletRequest request) {
        logger.info("JobTagController./by/id");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "fetched successfully!", jobTagService.getJobTagByUuid(id, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/change/status/{id}")
    public ResponseEntity<ApiResponse> changeStatus(HttpServletRequest request, @PathVariable("id") String id) {
        logger.info("JobTagController./change/status");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            Boolean status = jobTagService.changeStatus(id);
            String messageResponse = Boolean.TRUE.equals(status) ? "JobTag activated Successfully!" : "JobTag deactivated Successfully!";
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, messageResponse, null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }

    }


}
