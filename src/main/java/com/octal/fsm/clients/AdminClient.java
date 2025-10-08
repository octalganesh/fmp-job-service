package com.octal.fsm.clients;

import com.google.protobuf.Api;
import com.octal.fsm.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import static com.octal.fsm.common.CommonConstants.USER_NAME;

@FeignClient(name = "admin-service")
public interface AdminClient {

    @GetMapping(value = "/customer-profile/get/by/{id}")
    ResponseEntity<ApiResponse> getCustomerById(@PathVariable("id") String id);

    @GetMapping(value = "/customer-profile/job-details-lead-customer-details")
    ResponseEntity<ApiResponse> getJobDetailsWithLeadAndCustomerDetails(@RequestParam("customerId") String customerId, @RequestParam("leadSourceId") String leadSourceId, @RequestHeader(USER_NAME) String userName);

    @GetMapping(value = "/customer-profile/job-details")
    ResponseEntity<ApiResponse> getJobDetailsForCustomerInfo(@RequestParam("customerId") String customerId, @RequestParam("leadSourceId") String leadSourceId, @RequestParam("customerTypeId") String customerTypeId, @RequestHeader(USER_NAME) String userName);
}
