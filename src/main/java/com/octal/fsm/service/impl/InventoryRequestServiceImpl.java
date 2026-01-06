package com.octal.fsm.service.impl;

import com.octal.fsm.clients.QuickBookClientService;
import com.octal.fsm.dto.*;
import com.octal.fsm.entities.InventoryPart;
import com.octal.fsm.entities.InventoryRequest;
import com.octal.fsm.entities.InventoryRequestItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.helper.CodeGenerator;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.InventoryPartRepository;
import com.octal.fsm.repositories.InventoryRequestRepository;
import com.octal.fsm.service.InventoryRequestService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryRequestServiceImpl implements InventoryRequestService {

    @Autowired
    private InventoryRequestRepository inventoryRequestRepository;

    @Autowired
    private SpecificationFactory<InventoryRequest> inventoryRequestSpecificationFactory;
    @Autowired
    private InventoryPartRepository inventoryPartRepository;
    @Autowired
    private QuickBookClientService quickBookClientService;
    @Autowired
    private CodeGenerator codeGenerator;

    @Override
    public String createRequest(InventoryRequestDTO.Create requestDTO, Long tenantId, boolean isSuperAdmin) throws Exception {
        InventoryRequest request = new InventoryRequest();
        request.setTechnicianId(requestDTO.getTechnicianId());
        request.setTaskId(requestDTO.getTaskId());
        request.setComment(requestDTO.getComment());
        request.setRequestShowId(codeGenerator.generateRequestShowId());
        request.setStatus("PENDING");
        request.setRequestedAt(LocalDateTime.now());

        for (InventoryRequestDTO.Item itemDto : requestDTO.getItems()) {
            InventoryRequestItem item = new InventoryRequestItem();
            item.setInventoryRequest(request);
            item.setInventoryName(itemDto.getInventoryName());
            item.setInventoryListId(itemDto.getInventoryListId());
            item.setRequestedQty(itemDto.getQuantity());
            request.getItems().add(item);
        }
        InventoryRequest saved = inventoryRequestRepository.save(request);
        return saved.getUuid();
    }

    @Override
    public PageItem<InventoryRequestResponseDTO> getAllListRequest(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws Exception {
        try {
            if(!TextUtils.isEmpty(listRequest.getSearchText())){
                String trimmedText = listRequest.getSearchText().trim();
                listRequest.setSearchText(trimmedText);
            }
            GenericSpecificationsBuilder<InventoryRequest> builder = new GenericSpecificationsBuilder<>();
            Pageable pageable = null;
            if (Boolean.TRUE.equals(listRequest.getAsc())) {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
            } else {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
            }
            prepareTaskListSearchFilter(listRequest, builder, tenantId);
            Page<InventoryRequest> pageResult = inventoryRequestRepository.findAll(builder.build(), pageable);
            List<InventoryRequestResponseDTO> dtoList =
                    pageResult.getContent()
                            .stream()
                            .map(this::toDto)
                            .collect(Collectors.toList());

            return new PageItem<>(pageResult.getTotalPages(), pageResult.getTotalElements(), dtoList, listRequest.getPageNumber(),
                    listRequest.getPageSize());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public InventoryRequestResponseDTO approvalInventoryRequest(InventoryApprovalDTO approvalDTO, Long tenantId, boolean isSuperAdmin) throws Exception {
        try {
            InventoryRequest request = inventoryRequestRepository
                    .findByUuid(approvalDTO.getId())
                    .orElseThrow(() -> new CodeException("Request not found", ErrorCode.COMMON));

            if (!"PENDING".equals(request.getStatus())) {
                throw new CodeException("Request already processed", ErrorCode.COMMON);
            }

            if (approvalDTO.getAction().equalsIgnoreCase("Cancel")) {
                request.setStatus("CANCELLED");
                request.setApprovedBy(approvalDTO.getApprovedBy());
                request.setRejectionReason(approvalDTO.getRejectionReason());
                request.setApprovedAt(LocalDateTime.now());
                inventoryRequestRepository.save(request);
                return toDto(request);
            }

            if (approvalDTO.getItems() == null || approvalDTO.getItems().isEmpty()) {
                throw new CodeException("No items provided for approval", ErrorCode.COMMON);
            }

            Map<String, InventoryRequestItem> requestItemMap =
                    request.getItems().stream().collect(Collectors.toMap(InventoryRequestItem::getInventoryListId,
                            Function.identity()));

            List<String> inventoryListIds = approvalDTO.getItems().stream()
                    .map(InventoryApprovalDTO.ItemApproval::getInventoryListId)
                    .collect(Collectors.toList());

            Map<String, InventoryPart> inventoryMap =
                    inventoryPartRepository.findByListIdIn(inventoryListIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    InventoryPart::getListId,
                                    Function.identity()
                            ));

            for (InventoryApprovalDTO.ItemApproval approval : approvalDTO.getItems()) {
                InventoryRequestItem item = requestItemMap.get(approval.getInventoryListId());
                if (item == null) {
                    throw new CodeException("Item not found", ErrorCode.COMMON);
                }
                int approvedQty = approval.getApprovedQty() != null ? approval.getApprovedQty() : 0;
//                InventoryPart inventory = inventoryMap.get(item.getInventoryListId());
//                if (inventory == null) {
//                    throw new CodeException("Inventory not found", ErrorCode.COMMON);
//                }
//                int availableQty;
//                try {
//                    availableQty = Integer.parseInt(inventory.getQuantityOnHand());
//                } catch (Exception e) {
//                    throw new CodeException(
//                            "Invalid inventory quantity for " + item.getInventoryListId(),
//                            ErrorCode.COMMON
//                    );
//                }
//                if (availableQty < approvedQty) {
//                    throw new CodeException(
//                            "Insufficient stock for " + item.getInventoryListId(),
//                            ErrorCode.COMMON
//                    );
//                }
//                inventory.setQuantityOnHand(String.valueOf(availableQty - approvedQty));
                item.setApprovedQty(approvedQty);
            }
//            updateInventoryInQB(inventoryMap);
            request.setApprovedBy(approvalDTO.getApprovedBy());
            request.setStatus("APPROVED");
            request.setApprovedAt(LocalDateTime.now());
            inventoryRequestRepository.save(request);
            return toDto(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateInventoryInQB(Map<String, InventoryPart> inventoryMap) {
        inventoryPartRepository.saveAll(inventoryMap.values());// Update local inventory first
        List<InventoryPartDTO.Add> qbUpdateList = inventoryMap.values().stream()
                .map(inv -> {
                    InventoryPartDTO.Add dto = new InventoryPartDTO.Add();
                    dto.setListId(inv.getListId());
                    dto.setQuantityOnHand(inv.getQuantityOnHand());
//                    dto.setQuantityOnOrder(inv.getQuantityOnOrder());
//                    dto.setQuantityOnSalesOrder(inv.getQuantityOnSalesOrder());
                    return dto;
                })
                .collect(Collectors.toList());
        // Call to QuickBooks service to update inventory
        quickBookClientService.updateInventory(qbUpdateList, 1L);
    }

    private InventoryRequestResponseDTO toDto(InventoryRequest request) {

        InventoryRequestResponseDTO dto = new InventoryRequestResponseDTO();

        dto.setId(request.getUuid());
        dto.setTechnicianId(request.getTechnicianId());
        dto.setTaskId(request.getTaskId());
        dto.setComment(request.getComment());
        dto.setRequestShowId(request.getRequestShowId());
        dto.setRequestedAt(request.getRequestedAt().toString());
        dto.setApprovedAt(request.getApprovedAt() != null ? request.getApprovedAt().toString() : null);
        dto.setApprovedBy(request.getApprovedBy());
        dto.setStatus(request.getStatus());
        dto.setRejectionReason(request.getRejectionReason());

        List<InventoryRequestResponseDTO.Item> items = request.getItems().stream()
                .map(item -> {
                    InventoryRequestResponseDTO.Item i =
                            new InventoryRequestResponseDTO.Item();
                    i.setInventoryListId(item.getInventoryListId());
                    i.setInventoryName(item.getInventoryName());
                    i.setRequestedQty(item.getRequestedQty());
                    i.setApprovedQty(item.getApprovedQty());
                    return i;
                })
                .collect(Collectors.toList());
        dto.setItems(items);
        return dto;
    }


    private void prepareTaskListSearchFilter(com.octal.fsm.models.request.PageRequest.List listRequest, GenericSpecificationsBuilder<InventoryRequest> builder, Long tenantId) {
        builder.with(inventoryRequestSpecificationFactory.isEqual("deleted", false));
//        if (!TextUtils.isEmpty(tenantId)) {
//            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("tenantId", tenantId));
//        }

        if (listRequest.getTechnicianId() != null && !listRequest.getTechnicianId().isEmpty()) {
            builder.with(inventoryRequestSpecificationFactory.isEqual("technicianId", listRequest.getTechnicianId().get(0)));
        }

        if (!TextUtils.isEmpty(listRequest.getTaskId())) {
            builder.with(inventoryRequestSpecificationFactory.isEqual("taskId", listRequest.getTaskId()));
        }

        if (listRequest.getIsActive() != null) {
            builder.with(inventoryRequestSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (!TextUtils.isEmpty(listRequest.getStatus())) {
            builder.with(inventoryRequestSpecificationFactory.isEqual("status", listRequest.getStatus()));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(inventoryRequestSpecificationFactory.isGreaterThanOrEquals("startDate", listRequest.getStartDate()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(inventoryRequestSpecificationFactory.isLessThanOrEquals("endDate", listRequest.getEndDate()));
        }
    }

}
