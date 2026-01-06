package com.octal.fsm.service.impl;

import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobTagDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.JobTag;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobTagRepository;
import com.octal.fsm.service.GeneralSettingService;
import com.octal.fsm.service.JobTagService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JobTagServiceImpl implements JobTagService {

    @Autowired
    private JobTagRepository jobTagRepository;
    @Autowired
    private SpecificationFactory<JobTag> jobTagSpecificationFactory;
    @Autowired
    private GeneralSettingService generalSettingService;


    @Override
    public String addJobTag(JobTagDTO.Add add, Long tenantId, Boolean isSuperAdmin) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
        if (TextUtils.isEmpty(add.getName()))
            throw new CodeException("Tag name is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(add.getTagColor()))
            throw new CodeException("Tag color is required", ErrorCode.COMMON);
//        Optional<JobTag> optionalJobTag = jobTagRepository.findByUuid(add.getId());
//        if (optionalJobTag.isPresent() && !optionalJobTag.get().getUuid().equals(add.getId())) {
//            throw new CodeException("JobTag is already present!", ErrorCode.RECORD_NOT_FOUND);
//        }
        JobTag newJobTagRecord = null;
        if (TextUtils.isEmpty(add.getId())) {
            Boolean isTagExist = jobTagRepository.existsByNameAndTenantId(add.getName(), tenantId);
            if (isTagExist) {
                throw new CodeException("Tag name is already exist", ErrorCode.COMMON);
            }
            newJobTagRecord = new JobTag();
            newJobTagRecord.setCreatedAt(LocalDateTime.now());
            newJobTagRecord.setUpdatedAt(LocalDateTime.now());
            newJobTagRecord.setTenantId(tenantId);
        } else {
            Optional<JobTag> jobTag = jobTagRepository.findByUuid(add.getId());
            if (jobTag.isPresent()) {
                Boolean isTagExist = jobTagRepository.existsByNameAndTenantIdAndUuidNot(add.getName(), tenantId, add.getId());
                if (isTagExist) {
                    throw new CodeException("Tag name is already exist", ErrorCode.COMMON);
                }
                newJobTagRecord = jobTag.get();
                newJobTagRecord.setUpdatedAt(LocalDateTime.now());
            } else {
                throw new CodeException("jobType not Found!", ErrorCode.COMMON);
            }
        }
        newJobTagRecord.setActive(Boolean.TRUE.equals(add.isActive()));
        newJobTagRecord.setDeleted(false);
        newJobTagRecord.setName(add.getName());
        newJobTagRecord.setTagColor(add.getTagColor());
        JobTag jobTag = jobTagRepository.save
                (newJobTagRecord);
        return jobTag.getUuid();
    }

    @Override
    public Boolean deleteById(String id) throws CodeException {
        Optional<JobTag> jobTagRecord = jobTagRepository.findByUuid(id);
        if (jobTagRecord.isPresent()) {
            jobTagRecord.get().setDeleted(true);
            jobTagRepository.save(jobTagRecord.get());
            return true;
        } else {
            throw new CodeException(CommonConstants.JOB_TAG_NOT_FOUND + id, ErrorCode.COMMON);
        }
    }

    @Override
    public JobTagDTO.Detail getJobTagByUuid(String id, Long tenantId, Boolean isSuperAdmin) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
        Optional<JobTag> jobTagOptional = jobTagRepository.findByUuidAndTenantId(id, tenantId);
        if (jobTagOptional.isPresent()) {
            DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
            JobTagDTO.Detail jobType = new JobTagDTO.Detail();
            jobType.setName(jobTagOptional.get().getName());
            jobType.setTagColor(jobTagOptional.get().getTagColor());
            jobType.setId(jobTagOptional.get().getUuid());
            jobType.setIsActive(jobTagOptional.get().getActive());
            jobType.setCreatedAt(jobTagOptional.get().getCreatedAt() != null ? jobTagOptional.get().getCreatedAt().format(dateTimeFormatter) : null);
            return jobType;
        } else {
            throw new CodeException(CommonConstants.JOB_TAG_NOT_FOUND + id, ErrorCode.COMMON);
        }
    }

    @Override
    public Boolean changeStatus(String id) throws CodeException {
        Optional<JobTag> jobTagRecord = jobTagRepository.findByUuid(id);

        if (jobTagRecord.isPresent()) {
            JobTag jobTag = jobTagRecord.get();

            if (Boolean.TRUE.equals(jobTag.getActive())) {
                jobTag.setActive(false);
                jobTagRepository.save(jobTag);
                return false;
            } else {
                jobTag.setActive(true);
                jobTagRepository.save(jobTag);
                return true;
            }
        } else {
            throw new CodeException(CommonConstants.JOB_TAG_NOT_FOUND + id, ErrorCode.COMMON);
        }
    }


    @Override
    public PageItem<JobTagDTO.Detail> getAllJobTags(PageRequest.List listRequest, Long tenantId, Boolean isSuperAdmin) {
        if (isSuperAdmin)
            tenantId = 1L;
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
        GenericSpecificationsBuilder<JobTag> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            if (!TextUtils.isEmpty(listRequest.getSortBy())) {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getSortBy()).ascending());
            } else {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
            }
        } else {
            if (!TextUtils.isEmpty(listRequest.getSortBy())) {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getSortBy()).descending());
            } else {
                pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
            }
        }

        builder.with(jobTagSpecificationFactory.isEqual("tenantId", tenantId));

        prepareJobTagSearchFilter(listRequest, builder);
        Page<JobTag> pagedResult = jobTagRepository.findAll(builder.build(), pageable);
        List<JobTagDTO.Detail> responseList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
        for (JobTag jobTag : pagedResult.getContent()) {
            JobTagDTO.Detail dto = new JobTagDTO.Detail();
            dto.setId(jobTag.getUuid());
            dto.setName(jobTag.getName());
            dto.setTagColor(jobTag.getTagColor());
            dto.setIsActive(jobTag.getActive());
            dto.setCreatedAt(jobTag.getCreatedAt() != null ? jobTag.getCreatedAt().format(dateTimeFormatter) : null);
            dto.setUpdatedAt(jobTag.getUpdatedAt() != null ? jobTag.getUpdatedAt().format(dateTimeFormatter) : null);
            responseList.add(dto);
        }

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(),
                listRequest.getPageSize());
    }

    private void prepareJobTagSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<JobTag> builder) {

        builder.with(jobTagSpecificationFactory.isEqual("deleted", false));

        if (org.apache.commons.lang.StringUtils.isNotBlank(listRequest.getSearchText())) {
            builder.with(jobTagSpecificationFactory.like("name", listRequest.getSearchText()));
        }
        if (listRequest.getIsActive() != null) {
            builder.with(jobTagSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(jobTagSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(jobTagSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }

    }

}
