package com.octal.fsm.service;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.*;
import com.octal.fsm.entities.InventoryRequest;
import com.octal.fsm.models.request.PageRequest;
import org.springframework.http.ResponseEntity;

public interface InventoryRequestService {

    String createRequest(InventoryRequestDTO.Create requestDTO,Long tenantId,boolean isSuperAdmin) throws Exception;

    PageItem<InventoryRequestResponseDTO> getAllListRequest(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws Exception;

    InventoryRequestResponseDTO approvalInventoryRequest(InventoryApprovalDTO approvalDTO, Long tenantId, boolean isSuperAdmin) throws Exception;
}
