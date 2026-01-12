package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.PaymentListRequestDTO;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.JobInvoiceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/job-invoice")
public class JobInvoiceController extends BaseController {

    private static final Logger logger = LogManager.getLogger(JobInvoiceController.class);

    @Autowired
    JobInvoiceService jobInvService;

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getPayments(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean superAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Payment list successfully",
                    jobInvService.getInvoiceDataOfFrontOfficeUser(listRequest,tenantId,superAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving payments: {}", e.getMessage(), e);
            return handleException(e);
        }
    }
}
