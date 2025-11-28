package com.octal.fsm.clients;

import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.models.request.PageRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.octal.fsm.common.CommonConstants.USER_NAME;

@FeignClient(name = "technician-service")
public interface TechnicianClient {

    @GetMapping("/technician/get/by/{id}")
    ResponseEntity<ApiResponse> getTechnicianById(@PathVariable("id") String id,
                                                  @RequestHeader("userName") String userName);

    @PostMapping("/technician/get-by-ids")
    ResponseEntity<ApiResponse> getTechByIds(@RequestBody List<String> ids, @RequestHeader("tenantId") Long tenantId);

    @PostMapping("/technician/get-all")
    ResponseEntity<ApiResponse> getAllTechnician(@RequestBody PageRequest.List listRequest,@RequestHeader("tenantId") Long tenantId, @RequestHeader("superAdmin") boolean superAdmin);

    @GetMapping("/technician/get-by-uuid/{id}")
    ResponseEntity<ApiResponse> getTechnicianByUuid(@PathVariable("id") String id,@RequestHeader("tenantId") Long tenantId,@RequestHeader("superAdmin") boolean superAdmin);


}
