package com.octal.fsm.service;

import com.octal.fsm.dto.*;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;

public interface EstimateBillService {

    void saveEstimate(EstimateRequestDto.Create create,Long tenantId) throws CodeException;

    PageItem<EstimateResponseDto.list> getAllList(PageRequest.List pageRequest, Long tenantId, boolean isSuperAdmin) throws CodeException;

    EstimateDocumentResponseDto uploadDocuments(EstimateDocumentRequestDto request,Long tenantId) throws CodeException;
}
