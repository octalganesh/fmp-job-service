package com.octal.fsm.service.impl;

import com.octal.fsm.dto.*;
import com.octal.fsm.entities.EstimateBill;
import com.octal.fsm.entities.EstimateDocument;
import com.octal.fsm.entities.EstimateItem;
import com.octal.fsm.entities.InventoryPart;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.helper.CodeGenerator;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.EstimateBillRepository;
import com.octal.fsm.repositories.EstimateDocumentRepository;
import com.octal.fsm.service.EstimateBillService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstimateBillServiceImpl implements EstimateBillService {

    @Autowired
    private EstimateBillRepository estimateBillRepository;

    @Autowired
    private SpecificationFactory<EstimateBill> estimateBillSpecificationFactory;
    @Autowired
    private EstimateDocumentRepository estimateDocumentRepository;
    @Autowired
    private CodeGenerator codeGenerator;
    @Value("${aws.base-url}")
    private String awsS3BaseUrl;

    @Override
    public void saveEstimate(EstimateRequestDto.Create dto,Long tenantId) throws CodeException {
        EstimateBill estimate = new EstimateBill();
        estimate.setEstimateId(codeGenerator.generateEstimateId());
        estimate.setJobId(dto.getJobId());
        estimate.setTaskId(dto.getTaskId());
        estimate.setStatus(dto.getStatus());
        estimate.setSubTotal(dto.getSubTotal());
        estimate.setTaxPercent(dto.getTaxPercent());
        estimate.setTaxAmount(dto.getTaxAmount());
        estimate.setGrandTotal(dto.getGrandTotal());
        estimate.setTenantId(tenantId);
        estimate.setDocumentUrl(dto.getDocumentUrl());
        List<EstimateItem> items = dto.getItems().stream().map(i -> {
            EstimateItem item = new EstimateItem();
            item.setListId(i.getListId());
            item.setItemName(i.getItemName());
            item.setItemType(i.getItemType());
            item.setUnitCost(i.getUnitCost());
            item.setQuantity(i.getQuantity());
            item.setTotalValue(i.getTotalValue());
            item.setEstimate(estimate);
            return item;
        }).collect(Collectors.toList());

        estimate.setItems(items);

        estimateBillRepository.save(estimate);
    }

    @Override
    public PageItem<EstimateResponseDto.list> getAllList(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try{
            if(listRequest != null && listRequest.getSearchText() != null) {
                String trimmedText = listRequest.getSearchText().trim();
                listRequest.setSearchText(trimmedText);
            }
            GenericSpecificationsBuilder<EstimateBill> builder = new GenericSpecificationsBuilder<>();
            Pageable pageable = null;
            if (Boolean.TRUE.equals(listRequest.getAsc())) {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
            } else {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
            }
            prepareEstimateListSearchFilter(listRequest, builder, tenantId);
            Page<EstimateBill> estimateBills = estimateBillRepository.findAll(builder.build(), pageable);
            List<EstimateResponseDto.list> responseList = estimateBills.map(this::convertDTO).getContent();

            return new PageItem<>(estimateBills.getTotalPages(), estimateBills.getTotalElements(), responseList, listRequest.getPageNumber(), listRequest.getPageSize());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EstimateDocumentResponseDto uploadDocuments(EstimateDocumentRequestDto request,Long tenantId) throws CodeException {
        if(tenantId == null)
            tenantId = 1L;
        if(request.getEstimateId() == null){
            throw new CodeException("Estimate Id is required!", ErrorCode.COMMON);
        }
        EstimateBill estimate = estimateBillRepository.findByEstimateIdAndTenantId(request.getEstimateId(),tenantId)
                .orElseThrow(() -> new RuntimeException("Estimate not found"));
        EstimateDocument doc = new EstimateDocument();
        doc.setDocumentUrl(awsS3BaseUrl+request.getDocumentUrl());
        doc.setDocumentType(request.getDocumentType());
        doc.setEstimate(estimate);
        estimateDocumentRepository.save(doc);
        EstimateDocumentResponseDto response = new EstimateDocumentResponseDto();
        response.setDocumentUrl(doc.getDocumentUrl());
        response.setDocumentType(doc.getDocumentType());
        response.setEstimateId(request.getEstimateId());
        return response;
    }

    private EstimateResponseDto.list convertDTO(EstimateBill bill) {

        EstimateResponseDto.list dto = new EstimateResponseDto.list();
        dto.setEstimateId(bill.getEstimateId());
        dto.setJobId(bill.getJobId());
        dto.setTaskId(bill.getTaskId());
        dto.setStatus(bill.getStatus());
        dto.setSubTotal(bill.getSubTotal());
        dto.setTaxPercent(bill.getTaxPercent());
        dto.setTaxAmount(bill.getTaxAmount());
        dto.setGrandTotal(bill.getGrandTotal());
        dto.setActiveStatus(bill.getActive());
        dto.setTenantId(bill.getTenantId().toString());
        dto.setCreatedAt(bill.getCreatedAt().toString());
        // Map items
        List<EstimateRequestDto.EstimateItemDto> items =
                bill.getItems().stream().map(item -> {
                    EstimateRequestDto.EstimateItemDto i =
                            new EstimateRequestDto.EstimateItemDto();
                    i.setListId(item.getListId());
                    i.setItemName(item.getItemName());
                    i.setItemType(item.getItemType());
                    i.setUnitCost(item.getUnitCost());
                    i.setQuantity(item.getQuantity());
                    i.setTotalValue(item.getTotalValue());
                    return i;
                }).collect(Collectors.toList());

        List<EstimateDocumentResponseDto> collect = bill.getDocuments().stream().map(doc -> {
            EstimateDocumentResponseDto responseDto = new EstimateDocumentResponseDto();
            responseDto.setEstimateId(doc.getEstimate().getEstimateId());
            responseDto.setDocumentUrl(doc.getDocumentUrl());
            responseDto.setDocumentType(doc.getDocumentType());
            return responseDto;
        }).collect(Collectors.toList());
        dto.setEstimateBills(collect);
        dto.setItems(items);
        return dto;
    }


    private void prepareEstimateListSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<EstimateBill> builder, Long tenantId) {
        builder.with(estimateBillSpecificationFactory.isEqual("deleted", false));
//        if (!TextUtils.isEmpty(tenantId)) {
//            builder.with(inventoryPartSpecificationFactory.isEqual("tenantId", tenantId));
//        }
        if(listRequest.getJobId() != null){
            builder.with(estimateBillSpecificationFactory.isEqual("jobId", listRequest.getJobId()));
        }

        if (listRequest.getTaskId() != null) {
            builder.with(estimateBillSpecificationFactory.isEqual("taskId", listRequest.getTaskId()));
        }

        if (listRequest.getIsActive() != null) {
            builder.with(estimateBillSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }

        if (listRequest.getStartDate() != null) {
            builder.with(estimateBillSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(estimateBillSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }
    }
}
