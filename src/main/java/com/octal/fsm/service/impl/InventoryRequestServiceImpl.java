package com.octal.fsm.service.impl;

import com.octal.fsm.dto.InventoryRequestDTO;
import com.octal.fsm.dto.InventoryRequestResponseDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.InventoryRequest;
import com.octal.fsm.entities.InventoryRequestItem;
import com.octal.fsm.models.request.PageRequest;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryRequestServiceImpl implements InventoryRequestService {

    @Autowired
    private InventoryRequestRepository inventoryRequestRepository;

    @Autowired
    private SpecificationFactory<InventoryRequest> inventoryRequestSpecificationFactory;

    @Override
    public String createRequest(InventoryRequestDTO.Create requestDTO, Long tenantId,boolean isSuperAdmin) throws Exception {
        InventoryRequest request = new InventoryRequest();
        request.setTechnicianId(requestDTO.getTechnicianId());
        request.setTaskId(requestDTO.getTaskId());
        request.setStatus("PENDING");
        request.setRequestedAt(LocalDateTime.now());

        for (InventoryRequestDTO.Item itemDto : requestDTO.getItems()) {
            InventoryRequestItem item = new InventoryRequestItem();
            item.setInventoryRequest(request);
            item.setInventoryListId(itemDto.getInventoryListId());
            item.setRequestedQty(itemDto.getQuantity());
            request.getItems().add(item);
        }
        InventoryRequest saved = inventoryRequestRepository.save(request);
        return saved.getUuid();
    }

    @Override
    public PageItem<InventoryRequestResponseDTO> getAllListRequest(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws Exception {
        try{
            String trimmedText = listRequest.getSearchText().trim();
            listRequest.setSearchText(trimmedText);
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

    private InventoryRequestResponseDTO toDto(InventoryRequest request) {

        InventoryRequestResponseDTO dto = new InventoryRequestResponseDTO();

        dto.setRequestId(request.getUuid());
        dto.setTechnicianId(request.getTechnicianId());
        dto.setTaskId(request.getTaskId());
        dto.setStatus(request.getStatus());
        dto.setRequestedAt(request.getRequestedAt().toString());
        dto.setApprovedAt(request.getApprovedAt() != null ? request.getApprovedAt().toString() : null);
        dto.setApprovedBy(request.getApprovedBy());
        dto.setRejectionReason(request.getRejectionReason());

        List<InventoryRequestResponseDTO.Item> items = request.getItems().stream()
                        .map(item -> {
                            InventoryRequestResponseDTO.Item i =
                                    new InventoryRequestResponseDTO.Item();
                            i.setInventoryListId(item.getInventoryListId());
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
        if (!TextUtils.isEmpty(listRequest.getJobStatus())) {
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
