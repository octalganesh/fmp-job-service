package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.EstimateDocumentRequestDto;
import com.octal.fsm.dto.EstimateRequestDto;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.EstimateBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/estimates")
@RequiredArgsConstructor
public class EstimateBillController extends BaseController{

    private final EstimateBillService estimateService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createEstimate(@RequestBody EstimateRequestDto.Create listRequest, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            estimateService.saveEstimate(listRequest, tenantId);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Bill Added Success.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getAllList(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Data fetch successfully", estimateService.getAllList(listRequest, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/document-upload")
    public ResponseEntity<ApiResponse> createEstimate(@RequestBody EstimateDocumentRequestDto documentRequestDto, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            estimateService.uploadDocuments(documentRequestDto, tenantId);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Estimate Bill Uploaded Success.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return handleException(e);
        }
    }
}
