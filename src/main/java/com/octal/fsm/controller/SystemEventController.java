package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.SystemEventLogDTO;
import com.octal.fsm.service.SystemEventService;
import com.octal.fsm.service.impl.EventPublisherService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/event")
public class SystemEventController extends BaseController{

    private static final Logger logger = LogManager.getLogger(SystemEventController.class);

    @Autowired
    private SystemEventService systemEventService;

    @Autowired
    private EventPublisherService publisher;

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse> publishEvent(@RequestBody SystemEventLogDTO event) {
        try{
            publisher.publish(event);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Event published", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error publishing event: {}", e.getMessage(), e);
            return handleException(e);
        }

    }

    @GetMapping("/get-event-by-job-id/{referenceId}")
    public ResponseEntity<ApiResponse> getEvents(@PathVariable String referenceId, HttpServletRequest request) {
        try {
            Long tenantId = getTenantId(request);
            boolean isSuperAdmin = isSuperAdmin(request);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job list successfully", systemEventService.getEventsForReference(referenceId), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving Forms Details for technician: {}", e.getMessage(), e);
            return handleException(e);
        }
    }
}
