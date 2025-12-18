package com.octal.fsm.service.impl;

import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.HTMLFormDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.HTMLFormPage;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.HTMLFormPageRepository;
import com.octal.fsm.service.GeneralSettingService;
import com.octal.fsm.service.HTMLFormPageService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HTMLFormPageServiceImpl implements HTMLFormPageService {

    @Autowired
    private HTMLFormPageRepository htmlFormPageRepository;

    @Autowired
    private SpecificationFactory<HTMLFormPage> htmlPageSpecificationFactory;
    @Autowired
    private GeneralSettingService generalSettingService;


    @Override
    public PageItem<HTMLFormDTO.Details> getAllFormsPage(PageRequest.List listRequest, Long tenantId) {
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
        GenericSpecificationsBuilder<HTMLFormPage> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        prepareHTMLFormsTypeSearchFilter(listRequest, builder, tenantId);
        Page<HTMLFormPage> pagedResult = htmlFormPageRepository.findAll(builder.build(), pageable);
        List<HTMLFormDTO.Details> responseList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
        for (HTMLFormPage forms : pagedResult.getContent()) {
            HTMLFormDTO.Details dto = new HTMLFormDTO.Details();
            dto.setId(forms.getUuid());
            dto.setName(forms.getName());
            dto.setContent(forms.getContent());
            dto.setCreatedAt(forms.getCreatedAt().format(dateTimeFormatter));
            dto.setUpdatedAt(forms.getUpdatedAt().format(dateTimeFormatter));
            dto.setActive(forms.getActive());
            responseList.add(dto);
        }
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(),
                listRequest.getPageSize());
    }

    @Override
    public HTMLFormDTO.Details getById(String id) throws CodeException {
        Optional<HTMLFormPage> formstOptional = htmlFormPageRepository.findByUuidAndDeletedFalse(id);
        if (formstOptional.isPresent()) {
            HTMLFormDTO.Details detail = new HTMLFormDTO.Details();
            detail.setName(formstOptional.get().getName());
            detail.setId(formstOptional.get().getUuid());
            detail.setContent(formstOptional.get().getContent());
            detail.setActive(formstOptional.get().getActive());
            detail.setCreatedAt(formstOptional.get().getCreatedAt().toString());
            detail.setUpdatedAt(formstOptional.get().getUpdatedAt().toString());
            return detail;
        } else {
            throw new CodeException(CommonConstants.HTML_PAGE_NOT_FOUND + id, ErrorCode.COMMON);
        }
    }

    @Override
    public Boolean changeStatus(String id, Long tenantId) throws CodeException {
        Optional<HTMLFormPage> formsManagement = htmlFormPageRepository.findByUuidAndDeletedFalse(id);
        if (formsManagement.isPresent()) {
            if (Boolean.TRUE.equals(formsManagement.get().getActive())) {
                formsManagement.get().setActive(false);
                htmlFormPageRepository.save(formsManagement.get());
                return false;
            } else {
                formsManagement.get().setActive(true);
                htmlFormPageRepository.save(formsManagement.get());
                return true;
            }
        } else {
            throw new CodeException(CommonConstants.HTML_PAGE_NOT_FOUND + id, ErrorCode.COMMON);
        }
    }

    private void prepareHTMLFormsTypeSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<HTMLFormPage> builder, Long tenantId) {
        builder.with(htmlPageSpecificationFactory.isEqual("deleted", false));
//        builder.with(htmlPageSpecificationFactory.isEqual("tenantId", tenantId));
        if (org.apache.commons.lang.StringUtils.isNotBlank(listRequest.getSearchText())) {
            builder.with(htmlPageSpecificationFactory.like("name", listRequest.getSearchText()));
        }
        if (listRequest.getIsActive() != null) {
            builder.with(htmlPageSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }

        if (listRequest.getStartDate() != null) {
            builder.with(htmlPageSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(htmlPageSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }

    }
}
