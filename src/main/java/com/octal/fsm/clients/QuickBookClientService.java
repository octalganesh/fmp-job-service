package com.octal.fsm.clients;

import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.CustomerRestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "quickbook-sync-service")
public interface QuickBookClientService {

    @PostMapping("/api/rest/customer/create")
    ResponseEntity<ApiResponse> createCustomerQueue(@RequestBody CustomerRestDTO.CreateQueue createQueue, Long tenantId);
}
