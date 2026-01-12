package com.octal.fsm.clients;

import com.octal.fsm.dto.*;
import com.octal.fsm.entities.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


@FeignClient(name = "quickbook-sync-service")
public interface QuickBookClientService {

    @PostMapping("/api/rest/customer/create")
    ResponseEntity<ApiResponse> createCustomerQueue(@RequestBody CustomerRestDTO.CreateQueue createQueue, @RequestHeader("tenantId") Long tenantId);

    @PostMapping("/api/rest/invoice/create")
    ResponseEntity<ApiResponse> createInvoiceQueue(@RequestBody InvoiceRestDTO.Add add, @RequestHeader("tenantId") Long tenantId);

    @PostMapping("/api/rest/inventory-type/update-quantity")
    ResponseEntity<ApiResponse> updateInventory(@RequestBody List<InventoryPartDTO.Add> add, @RequestHeader("tenantId") Long tenantId);
}
