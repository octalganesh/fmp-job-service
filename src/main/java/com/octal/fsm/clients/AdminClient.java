package com.octal.fsm.clients;

import com.google.protobuf.Api;
import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.EmailTemplateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static com.octal.fsm.common.CommonConstants.USER_NAME;

@FeignClient(name = "admin-service")
public interface AdminClient {

    @GetMapping(value = "/customer-profile/get/by/{id}")
    ResponseEntity<ApiResponse> getCustomerById(@PathVariable("id") String id);

    @GetMapping(value = "/customer-profile/job-details-lead-customer-details")
    ResponseEntity<ApiResponse> getJobDetailsWithLeadAndCustomerDetails(@RequestParam("customerId") String customerId, @RequestParam("leadSourceId") String leadSourceId, @RequestHeader(USER_NAME) String userName);

    @GetMapping(value = "/customer-profile/job-details")
    ResponseEntity<ApiResponse> getJobDetailsForCustomerInfo(@RequestParam("customerId") String customerId, @RequestParam("leadSourceId") String leadSourceId, @RequestParam("customerTypeId") String customerTypeId, @RequestHeader(USER_NAME) String userName);


    @PostMapping("/email-template/get-template-content")
    ResponseEntity<ApiResponse> getTemplateContent(@RequestBody EmailTemplateDto.EmailTemplateRequest request, @RequestHeader(USER_NAME) String userName);

}
