package com.octal.fsm.service.impl;


import com.octal.fsm.dto.InventoryPartDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.InventoryPart;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.InventoryPartRepository;
import com.octal.fsm.service.InventoryPartService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class InventoryPartServiceImpl implements InventoryPartService {

    @Autowired
    private InventoryPartRepository inventoryPartRepository;

    @Autowired
    private SpecificationFactory<InventoryPart> inventoryPartSpecificationFactory;

    public void saveInventoryData(List<InventoryPartDTO.Add> addList) {
        if (addList == null || addList.isEmpty()) {
            return;
        }
        try {
            List<String> listIds = addList.stream()
                    .map(InventoryPartDTO.Add::getListId)
                    .collect(Collectors.toList());

            Set<String> existingListIds = inventoryPartRepository.findByListIdIn(listIds)
                    .stream()
                    .map(InventoryPart::getListId)
                    .collect(Collectors.toSet());

            List<InventoryPart> entitiesToSave = new ArrayList<>();
            for (InventoryPartDTO.Add dto : addList) {
                if (!existingListIds.contains(dto.getListId())) {
                    entitiesToSave.add(toEntity(dto));
                }
            }
            if (!entitiesToSave.isEmpty()) {
                inventoryPartRepository.saveAll(entitiesToSave);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public PageItem<InventoryPartDTO.Response> getAllInventoryList(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try{
            if(listRequest != null && listRequest.getSearchText() != null) {
                String trimmedText = listRequest.getSearchText().trim();
                listRequest.setSearchText(trimmedText);
            }
            GenericSpecificationsBuilder<InventoryPart> builder = new GenericSpecificationsBuilder<>();
            Pageable pageable = null;
            if (Boolean.TRUE.equals(listRequest.getAsc())) {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
            } else {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
            }
            prepareInventoryListSearchFilter(listRequest, builder, tenantId);
            Page<InventoryPart> inventoryParts = inventoryPartRepository.findAll(builder.build(), pageable);
            List<InventoryPartDTO.Response> responseList = inventoryParts.map(this::convertDTO).getContent();

            return new PageItem<>(inventoryParts.getTotalPages(), inventoryParts.getTotalElements(), responseList, listRequest.getPageNumber(), listRequest.getPageSize());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void prepareInventoryListSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<InventoryPart> builder, Long tenantId) {
        builder.with(inventoryPartSpecificationFactory.isEqual("deleted", false));
//        if (!TextUtils.isEmpty(tenantId)) {
//            builder.with(inventoryPartSpecificationFactory.isEqual("tenantId", tenantId));
//        }

        if (listRequest.getListId() != null) {
            builder.with(inventoryPartSpecificationFactory.isEqual("listId", listRequest.getListId()));
        }

        if (listRequest.getIsActive() != null) {
            builder.with(inventoryPartSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (!TextUtils.isEmpty(listRequest.getSearchText())) {
            builder.with(inventoryPartSpecificationFactory.like("name", listRequest.getSearchText()).or(inventoryPartSpecificationFactory.like("fullName", listRequest.getSearchText())));
        }

        if (listRequest.getStartDate() != null) {
            builder.with(inventoryPartSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(inventoryPartSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }
    }

    private InventoryPart toEntity(InventoryPartDTO.Add dto) {
        InventoryPart part = new InventoryPart();

        part.setListId(dto.getListId());
        part.setTimeCreated(dto.getTimeCreated());
        part.setTimeModified(dto.getTimeModified());
        part.setEditSequence(dto.getEditSequence());
        part.setName(dto.getName());
        part.setFullName(dto.getFullName());
        if( dto.getIsActive() == null || dto.getIsActive().isEmpty()){
            part.setActive(true);
        } else {
            part.setActive(Boolean.valueOf(dto.getIsActive()));
        }
        part.setSublevel(dto.getSublevel());
        part.setSalesTaxCodeListId(dto.getSalesTaxCodeListId());
        part.setSalesTaxCodeFullName(dto.getSalesTaxCodeFullName());
        part.setSalesOrPurchasePrice(dto.getSalesOrPurchasePrice());
        part.setSalesOrPurchaseAccountFullName(dto.getSalesOrPurchaseAccountFullName());
        part.setSalesOrPurchaseAccountListId(dto.getSalesOrPurchaseAccountListId());
        part.setDiscountRatePercentage(dto.getDiscountRatePercentage());
        part.setAccountListId(dto.getAccountListId());
        part.setAccountFullName(dto.getAccountFullName());
        part.setIncomeAccountListId(dto.getIncomeAccountListId());
        part.setIncomeAccountFullName(dto.getIncomeAccountFullName());
        part.setCogsAccountListId(dto.getCogsAccountListId());
        part.setCogsAccountFullName(dto.getCogsAccountFullName());
        part.setPrefVendorListId(dto.getPrefVendorListId());
        part.setPrefVendorFullName(dto.getPrefVendorFullName());
        part.setAssetAccountListId(dto.getAssetAccountListId());
        part.setAssetAccountFullName(dto.getAssetAccountFullName());
        part.setSalesDesc(dto.getSalesDesc());
        part.setSalesPrice(dto.getSalesPrice());
        part.setPurchaseDesc(dto.getPurchaseDesc());
        part.setPurchaseCost(dto.getPurchaseCost());
        part.setReorderPoint(dto.getReorderPoint());
        part.setQuantityOnHand(dto.getQuantityOnHand());
        part.setAverageCost(dto.getAverageCost());
        part.setQuantityOnOrder(dto.getQuantityOnOrder());
        part.setQuantityOnSalesOrder(dto.getQuantityOnSalesOrder());
        part.setItemDesc(dto.getItemDesc());
        part.setTaxRate(dto.getTaxRate());
        part.setTaxVendorListId(dto.getTaxVendorListId());
        part.setTaxVendorFullName(dto.getTaxVendorFullName());
        part.setSalePrice(dto.getSalePrice());
        part.setItemType(dto.getItemType());

        return part;
    }

    private InventoryPartDTO.Response convertDTO(InventoryPart part) {
        InventoryPartDTO.Response dto = new InventoryPartDTO.Response();
        dto.setId(part.getUuid());
        dto.setListId(part.getListId());
        dto.setTimeCreated(part.getTimeCreated());
        dto.setTimeModified(part.getTimeModified());
        dto.setEditSequence(part.getEditSequence());
        dto.setName(part.getName());
        dto.setFullName(part.getFullName());
        dto.setActive(part.getActive());
        dto.setSublevel(part.getSublevel());
        dto.setSalesTaxCodeListId(part.getSalesTaxCodeListId());
        dto.setSalesTaxCodeFullName(part.getSalesTaxCodeFullName());
        dto.setSalesOrPurchasePrice(part.getSalesOrPurchasePrice());
        dto.setSalesOrPurchaseAccountFullName(part.getSalesOrPurchaseAccountFullName());
        dto.setSalesOrPurchaseAccountListId(part.getSalesOrPurchaseAccountListId());
        dto.setDiscountRatePercentage(part.getDiscountRatePercentage());
        dto.setAccountListId(part.getAccountListId());
        dto.setAccountFullName(part.getAccountFullName());
        dto.setIncomeAccountListId(part.getIncomeAccountListId());
        dto.setIncomeAccountFullName(part.getIncomeAccountFullName());
        dto.setCogsAccountListId(part.getCogsAccountListId());
        dto.setCogsAccountFullName(part.getCogsAccountFullName());
        dto.setPrefVendorListId(part.getPrefVendorListId());
        dto.setPrefVendorFullName(part.getPrefVendorFullName());
        dto.setAssetAccountListId(part.getAssetAccountListId());
        dto.setAssetAccountFullName(part.getAssetAccountFullName());
        dto.setSalesDesc(part.getSalesDesc());
        dto.setSalesPrice(part.getSalesPrice());
        dto.setPurchaseDesc(part.getPurchaseDesc());
        dto.setPurchaseCost(part.getPurchaseCost());
        dto.setReorderPoint(part.getReorderPoint());
        dto.setQuantityOnHand(part.getQuantityOnHand());
        dto.setAverageCost(part.getAverageCost());
        dto.setQuantityOnOrder(part.getQuantityOnOrder());
        dto.setQuantityOnSalesOrder(part.getQuantityOnSalesOrder());
        dto.setItemDesc(part.getItemDesc());
        dto.setTaxRate(part.getTaxRate());
        dto.setTaxVendorListId(part.getTaxVendorListId());
        dto.setTaxVendorFullName(part.getTaxVendorFullName());
        dto.setSalePrice(part.getSalePrice());
        dto.setItemType(part.getItemType());
        dto.setCreatedAt(part.getCreatedAt() != null ? part.getCreatedAt().toString() : null);
        return dto;
    }


}