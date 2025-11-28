package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.InvoiceRequest;
import com.octal.fsm.dto.QuickBookApiResponse;
import com.octal.fsm.dto.QuickBookDTO;
import com.octal.fsm.dto.QuickBooksItemDTO;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.service.impl.QuickBooksCustomerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/quickbooks")
public class QuickBooksController extends BaseController {
    private static final Logger logger = LogManager.getLogger(QuickBooksController.class);

    private final QuickBooksCustomerService customerService;

    public QuickBooksController(QuickBooksCustomerService customerService) {
        this.customerService = customerService;
    }


    @PostMapping("/create-customer")
    public ResponseEntity<ApiResponse> createJob(@Valid @RequestBody QuickBookDTO.CreateCustomer quickBookDTO, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Customer created in Quick books", customerService.addNewCustomer(quickBookDTO), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ApiResponse(Boolean.FALSE, e.getMessage(), null, "101", HttpStatus.OK), HttpStatus.OK);
        }
    }

//    @PostMapping("/customer")
//    public ResponseEntity<QuickBookApiResponse> createCustomer(@RequestBody CustomerRequest customerRequest) throws Exception {
//        QuickBookApiResponse response = customerService.createCustomer(customerRequest);
//        return ResponseEntity.ok(response);
//    }

    // Invoice creation API
    @PostMapping("/invoice")
    public ResponseEntity<ApiResponse> createInvoice(@RequestBody InvoiceRequest invoiceRequest, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Invoice created in Quick books successfully", customerService.createInvoice(invoiceRequest), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/invoice/{invoiceId}/send")
    public ResponseEntity<ApiResponse> sendInvoice(@PathVariable String invoiceId, @RequestParam(required = false) String sendTo, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Invoice sent successfully in QuickBooks.", customerService.sendInvoice(invoiceId, sendTo), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }


    @GetMapping("/item/all")
    public ResponseEntity<?> getAllItems(@RequestParam(defaultValue = "1") int startPosition, @RequestParam(defaultValue = "5") int maxResults, HttpServletRequest request) {
        try {
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Fetched data successfully", customerService.getAllItems(startPosition, maxResults), "200", HttpStatus.OK), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/item/create")
    public ResponseEntity<?> createItem(@RequestBody QuickBooksItemDTO itemDTO) throws Exception {
        QuickBookApiResponse response = customerService.createItem(itemDTO);
        return ResponseEntity.ok(response);
    }


}
