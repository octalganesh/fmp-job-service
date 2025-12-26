package com.octal.fsm.clients;

import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.CustomerRestDTO;
import com.octal.fsm.dto.InvoiceRestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "quickbook-sync-service")
public interface QuickBookClientService {

    @PostMapping("/api/rest/customer/create")
    ResponseEntity<ApiResponse> createCustomerQueue(@RequestBody CustomerRestDTO.CreateQueue createQueue, @RequestHeader("tenantId") Long tenantId);

    @PostMapping("/api/rest/invoice/create")
    ResponseEntity<ApiResponse> createInvoiceQueue(@RequestBody InvoiceRestDTO.Add add, @RequestHeader("tenantId") Long tenantId);
}
