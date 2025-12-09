package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.AppointmentDTO;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.AppointmentService;
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

@RequestMapping("/appointments")
@RestController
public class AppointmentController extends BaseController {

    private static final Logger logger = LogManager.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addAppointment(@RequestBody AppointmentDTO.Add add, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Appointment added successfully", appointmentService.addAppointment(add, userName), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }


    @PostMapping("/list")
    public ResponseEntity<ApiResponse> listAllAppointments(@RequestBody PageRequest.List list, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            Boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Appointment added successfully", appointmentService.listAllAppointments(list,tenantId,isSuperAdmin, userName), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/list/by-technician-id")
    public ResponseEntity<ApiResponse> listAllAppointmentsByTechnicianId(@RequestBody PageRequest.List list, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            Long tenantId = getTenantId(request);
            Boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Appointment added successfully", appointmentService.listAllAppointmentsWithTechnicianId(list,tenantId,isSuperAdmin, userName), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job task status: {}", e.getMessage(), e);
            return handleException(e);
        }
    }


}
