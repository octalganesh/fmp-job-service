package com.octal.fsm.clients;

import com.octal.fsm.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "technician-service")
public interface TechnicianClient {

    @GetMapping("/technician/get/by/{id}")
    ResponseEntity<ApiResponse> getTechnicianById(@PathVariable("id") String id,
                                                  @RequestHeader("userName") String userName);
}
