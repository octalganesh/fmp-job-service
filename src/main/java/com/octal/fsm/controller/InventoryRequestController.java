package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.InventoryApprovalDTO;
import com.octal.fsm.dto.InventoryRequestDTO;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.InventoryRequestService;
import com.octal.fsm.service.impl.JobServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/inventory-requests")
public class InventoryRequestController extends BaseController{

    private static final Logger logger = LogManager.getLogger(InventoryRequestController.class);

    @Autowired
    private InventoryRequestService inventoryRequestService;

    @PostMapping("/create-inventory-request")
    public ResponseEntity<ApiResponse> createRequestInventory(@RequestBody InventoryRequestDTO.Create create, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Inventory Created Successfully.", inventoryRequestService.createRequest(create, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating inventory request: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/get-inventory-request")
    public ResponseEntity<ApiResponse> getList(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Inventory data fetch Successfully.", inventoryRequestService.getAllListRequest(listRequest, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while fetch inventory request data: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/approved-inventory-request")
    public ResponseEntity<ApiResponse> approved(@RequestBody InventoryApprovalDTO approvalDTO, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            String msg = approvalDTO.getAction().equalsIgnoreCase("CANCEL") ? "Inventory request Cancelled Successfully." : "Inventory request Approved Successfully.";
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, msg, inventoryRequestService.approvalInventoryRequest(approvalDTO, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while approved inventory request data: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

}
