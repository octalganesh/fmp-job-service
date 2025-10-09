package com.octal.fsm.client;

import com.octal.fsm.dto.CustomerRequest;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "quickbooksClient", url = "${quickbooks.api.host}")
public interface QuickBooksClient {

    @PostMapping(value = "/v3/company/{companyId}/customer", consumes = "application/json", produces = "application/json")
    String createCustomer(@RequestHeader("Authorization") String bearerToken, @RequestParam("minorversion") String minorVersion, @RequestBody String customerJson,  // <-- now raw JSON
                          @PathVariable("companyId") String companyId);

    @PostMapping(value = "/v3/company/{companyId}/invoice", consumes = "application/json", produces = "application/json")
    String createInvoice(@RequestHeader("Authorization") String bearerToken, @RequestParam("minorversion") String minorVersion, @RequestBody String invoiceJson, @PathVariable("companyId") String companyId);


    @PostMapping(value = "/v3/company/{companyId}/invoice/{invoiceId}/send", consumes = "application/octet-stream", produces = "application/json")
    String sendInvoice(@RequestHeader("Authorization") String bearerToken, @PathVariable("companyId") String companyId, @PathVariable("invoiceId") String invoiceId, @RequestParam(value = "sendTo", required = false) String sendTo, @RequestParam("minorversion") String minorVersion);


    @PostMapping(value = "/v3/company/{realmId}/item", consumes = "application/json", produces = "application/json")
    @Headers("Content-Type: application/json")
    String createItem(@RequestHeader("Authorization") String bearerToken, @PathVariable("realmId") String realmId, @RequestBody String payload, @RequestParam("minorversion") String minorVersion);


    @PostMapping(value = "/v3/company/{realmId}/query", consumes = "application/text", produces = "application/json")
    @Headers("Content-Type: application/text")
    String queryItems(@RequestHeader("Authorization") String bearerToken, @PathVariable("realmId") String realmId, @RequestBody String query, @RequestParam("minorversion") String minorVersion);
}