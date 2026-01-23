package com.octal.fsm.service;

import com.octal.fsm.dto.PageItem;
import com.octal.fsm.dto.PaymentListRequestDTO;
import com.octal.fsm.dto.PaymentResponseDTO;
import com.octal.fsm.dto.TransactionResponseDTO;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;
import org.springframework.stereotype.Service;

@Service
public interface JobInvoiceService {
    PageItem<PaymentResponseDTO> getInvoiceDataOfFrontOfficeUser(PageRequest.List listRequest,Long tenantId, boolean isSuperAdmin)throws CodeException;

    TransactionResponseDTO getTrxData(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin)throws CodeException;
}
