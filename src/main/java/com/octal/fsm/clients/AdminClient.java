package com.octal.fsm.clients;


import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.EmailTemplateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.octal.fsm.common.CommonConstants.USER_NAME;

@FeignClient(name = "admin-service")
public interface AdminClient {

    @GetMapping(value = "/customer-profile/get/by-for-technician/{id}")
    ResponseEntity<ApiResponse> getCustomerById(@PathVariable("id") String id);

    @GetMapping(value = "/customer-profile/job-details-lead-customer-details")
    ResponseEntity<ApiResponse> getJobDetailsWithLeadAndCustomerDetails(@RequestParam("customerId") String customerId, @RequestParam("leadSourceId") String leadSourceId, @RequestHeader(USER_NAME) String userName, @RequestHeader("tenantId") Long tenantId,
                                                                        @RequestHeader("superAdmin") boolean superAdmin);

    @GetMapping(value = "/customer-profile/job-details")
    ResponseEntity<ApiResponse> getJobDetailsForCustomerInfo(@RequestParam("customerId") String customerId, @RequestParam("leadSourceId") String leadSourceId, @RequestParam("customerTypeId") String customerTypeId, @RequestHeader(USER_NAME) String userName, @RequestHeader("tenantId") Long tenantId,
                                                             @RequestHeader("superAdmin") boolean superAdmin);


    @PostMapping("/email-template/get-template-content")
    ResponseEntity<ApiResponse> getTemplateContent(@RequestBody EmailTemplateDto.EmailTemplateRequest request, @RequestHeader(USER_NAME) String userName, @RequestHeader("tenantId") Long tenantId,
                                                   @RequestHeader("superAdmin") boolean superAdmin);

    @GetMapping("/customer-feedback/get-feedback-by-taskId/{jobTaskId}")
    ResponseEntity<ApiResponse> getFeedbackByJobTaskId(@PathVariable("jobTaskId") String jobTaskId, @RequestHeader(USER_NAME) String userName);

    @GetMapping("/front-office/getFrontOfficeDevices")
    ResponseEntity<ApiResponse> getFrontOfficeDevices(@RequestParam String id, @RequestHeader("tenantId") Long tenantId);

    @GetMapping("/formsManagement/get-form-byType/{formTypeId}")
    ResponseEntity<ApiResponse> getFormByJobTypeId(@PathVariable("formTypeId") String formTypeId, @RequestHeader("tenantId") Long tenantId);

    @PostMapping("/customer-profile/customer-by-ids")
    ResponseEntity<ApiResponse> getCustomerByIds(@RequestBody List<String> ids, @RequestHeader("tenantId") Long tenantId,
                                                 @RequestHeader("superAdmin") boolean superAdmin);

    @GetMapping("/front-office/get-front-office-by-id/{id}")
    ResponseEntity<ApiResponse> getFrontOfficeById(@PathVariable("id") String id, @RequestHeader("tenantId") Long tenantId);

    @GetMapping("/general-setting/get-setting/{key}")
    ResponseEntity<com.octal.fsm.common.ApiResponse> getGeneralSettingBYKey(@PathVariable("key") String key, @RequestHeader("tenantId") Long tenantId);

    @GetMapping("/general-setting/get-setting-map")
    ResponseEntity<com.octal.fsm.common.ApiResponse> getGeneralSettingMap(@RequestHeader("tenantId") Long tenantId);

}
