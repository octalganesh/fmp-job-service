package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.HTMLFormPageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/html-page")
public class HTMLFormPageController extends BaseController {

    private static final Logger logger = LogManager.getLogger(HTMLFormPageController.class);

    @Autowired
    private HTMLFormPageService htmlFormPageService;

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getHtmlFormList(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        logger.info("HTMLFormPageController./getHtmlFormList");
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, CommonConstants.DETAILS_FETCHED, htmlFormPageService.getAllFormsPage(listRequest, tenantId), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping(value = "/get/by/{id}")
    public ResponseEntity<ApiResponse> getHtmlFormById(@PathVariable("id") String id, HttpServletRequest request) {
        logger.info("HTMLFormPageController./by/id");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "fetched successfully!", htmlFormPageService.getById(id), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/change/status/{id}")
    public ResponseEntity<ApiResponse> changeStatus(HttpServletRequest request, @PathVariable("id") String id) {
        logger.info("HTMLFormPageController./change/status");
        String userName = request.getHeader(CommonConstants.USER_NAME);
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            Boolean status = htmlFormPageService.changeStatus(id, tenantId);
            String messageResponse = Boolean.TRUE.equals(status) ? "HTML from page activated Successfully!" : "HTML from page deactivated Successfully!";
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, messageResponse, null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }

    }

}
