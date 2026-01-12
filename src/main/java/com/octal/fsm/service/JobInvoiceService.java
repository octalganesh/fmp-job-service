package com.octal.fsm.service;

import com.octal.fsm.dto.PageItem;
import com.octal.fsm.dto.PaymentListRequestDTO;
import com.octal.fsm.dto.PaymentResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface JobInvoiceService {
    PageItem<PaymentResponseDTO> getInvoiceDataofFrontOfficeUser(String userId, PaymentListRequestDTO paymentListRequestDTO,
                                                                 String dateFormat);
}
