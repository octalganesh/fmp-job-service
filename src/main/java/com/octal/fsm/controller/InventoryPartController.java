package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.InventoryPartDTO;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.InventoryPartService;
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
import java.util.List;

@RestController
@RequestMapping("inventory")
public class InventoryPartController extends BaseController{

    private static final Logger logger = LogManager.getLogger(InventoryPartController.class);

    @Autowired
    private InventoryPartService inventoryPartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addInventory(@RequestBody List<InventoryPartDTO.Add> addList , HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            inventoryPartService.saveInventoryData(addList);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Data saved successfully",null , "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while saving Inventory Details : {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getAllList(@RequestBody PageRequest.List listRequest, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Data fetch successfully", inventoryPartService.getAllInventoryList(listRequest, tenantId, isSuperAdmin), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving Inventory Details : {}", e.getMessage(), e);
            return handleException(e);
        }
    }

}
