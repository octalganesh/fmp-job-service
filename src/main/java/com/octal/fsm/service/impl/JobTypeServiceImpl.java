package com.octal.fsm.service.impl;

import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobTaskDTO;
import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobTypeRepository;
import com.octal.fsm.service.JobTypeService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JobTypeServiceImpl implements JobTypeService {

    @Autowired
    private JobTypeRepository jobTypeRepository;
    @Autowired
    private SpecificationFactory<JobType> jobTypeSpecificationFactory;

    @Override
    public String addJobType(JobTypeDTO.Add add) throws CodeException {
        if (TextUtils.isEmpty(add.getName()))
            throw new CodeException("type name is required", ErrorCode.COMMON);
        Optional<JobType> optionalJobType = jobTypeRepository.findByUuid(add.getId());
        if (optionalJobType.isPresent() && !optionalJobType.get().getUuid().equals(add.getId())) {
            throw new CodeException("jobType is already present!", ErrorCode.RECORD_NOT_FOUND);
        }
        JobType newJobTypeRecord = null;
        if (TextUtils.isEmpty(add.getId())) {
            newJobTypeRecord = new JobType();
            newJobTypeRecord.setCreatedAt(LocalDateTime.now());
            newJobTypeRecord.setUpdatedAt(LocalDateTime.now());
        } else {
            Optional<JobType> jobType = jobTypeRepository.findByUuid(add.getId());
            if (jobType.isPresent()) {
                newJobTypeRecord = jobType.get();
                newJobTypeRecord.setUpdatedAt(LocalDateTime.now());
            } else {
                throw new CodeException("jobType not Found!", ErrorCode.COMMON);
            }
        }
        newJobTypeRecord.setActive(add.getIsActive());
        newJobTypeRecord.setDeleted(false);
        newJobTypeRecord.setName(add.getName());
        newJobTypeRecord.setDescription(add.getDescription());
        if(add.getJobTasks()!=null){
            newJobTypeRecord.setJobTasks(add.getJobTasks().stream()
                    .map(dto -> {
                        com.octal.fsm.entities.JobTask entity = new com.octal.fsm.entities.JobTask();
                        entity.setName(dto.getName());
                        entity.setDescription(dto.getDescription());
                        return entity;
                    })
                    .collect(java.util.stream.Collectors.toList()));
        }
        JobType jobType = jobTypeRepository.save
                (newJobTypeRecord);
        return jobType.getUuid();
    }

    @Override
    public Boolean deleteById(String id) throws CodeException {
        Optional<JobType> jobTypeRecord = jobTypeRepository.findByUuid(id);
        if (jobTypeRecord.isPresent()) {
            jobTypeRecord.get().setDeleted(true);
            jobTypeRepository.save(jobTypeRecord.get());
            return true;
        } else {
            throw new CodeException(CommonConstants.JOB_TYPE_NOT_FOUND + id, ErrorCode.COMMON);
        }
    }

    @Override
    public JobTypeDTO.Detail getJobTypeByUuid(String id) throws CodeException {
        Optional<JobType> jobTypeOptional = jobTypeRepository.findByUuid(id);
        if (jobTypeOptional.isPresent()) {
            JobTypeDTO.Detail jobType = new JobTypeDTO.Detail();
            jobType.setName(jobTypeOptional.get().getName());
            jobType.setId(jobTypeOptional.get().getUuid());
            jobType.setIsActive(jobTypeOptional.get().getActive());
            jobType.setCreatedAt(jobTypeOptional.get().getCreatedAt().toString());
            return jobType;
        } else {
            throw new CodeException(CommonConstants.JOB_TYPE_NOT_FOUND + id, ErrorCode.COMMON);
        }
    }

    @Override
    public Boolean changeStatus(String id) throws CodeException {
        Optional<JobType> jobTypeRecord = jobTypeRepository.findByUuid(id);
        if (jobTypeRecord.isPresent()) {
            if (Boolean.TRUE.equals(jobTypeRecord.get().getActive())) {
                jobTypeRecord.get().setActive(false);
                jobTypeRepository.save(jobTypeRecord.get());
                return false;
            } else {
                jobTypeRecord.get().setActive(true);
                jobTypeRepository.save(jobTypeRecord.get());
                return true;
            }
        } else {
            throw new CodeException(CommonConstants.JOB_TYPE_NOT_FOUND + id, ErrorCode.COMMON);
        }
    }

    @Override
    public PageItem<JobTypeDTO.Detail> getAllJobTypes(PageRequest.List listRequest) {
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        GenericSpecificationsBuilder<JobType> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        prepareJobTypeSearchFilter(listRequest, builder);
        Page<JobType> pagedResult = jobTypeRepository.findAll(builder.build(), pageable);
        List<JobTypeDTO.Detail> responseList = new ArrayList<>();
        for(JobType jobType:pagedResult.getContent()){
            JobTypeDTO.Detail dto=new JobTypeDTO.Detail();
            dto.setId(jobType.getUuid());
            dto.setName(jobType.getName());
            dto.setIsActive(jobType.getActive());
            dto.setCreatedAt(String.valueOf(jobType.getCreatedAt()));
            dto.setUpdatedAt(String.valueOf(jobType.getUpdatedAt()));
            dto.setDescription(jobType.getDescription());
            dto.setJobTasks(jobType.getJobTasks().stream()
                    .map(entity -> {
                        JobTaskDTO.Detail taskDto = new JobTaskDTO.Detail();
                        taskDto.setId(entity.getUuid());
                        taskDto.setName(entity.getName());
                        taskDto.setDescription(entity.getDescription());
                        taskDto.setCreatedAt(entity.getCreatedAt().toString());
                        taskDto.setUpdatedAt(entity.getUpdatedAt().toString());
                        return taskDto;
                    })
                    .collect(java.util.stream.Collectors.toList()));
            responseList.add(dto);
        }

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(),
                listRequest.getPageSize());
    }

    @Override
    public List<JobTypeDTO.Detail> getAllJobs() {
        List<JobType> jobTypes= jobTypeRepository.findAll();
        List<JobTypeDTO.Detail>jobTypeDTOS=new ArrayList<>();
        for(JobType jobType:jobTypes){
            JobTypeDTO.Detail dto=new JobTypeDTO.Detail();
            dto.setId(jobType.getUuid());
            dto.setName(jobType.getName());
            dto.setIsActive(jobType.getActive());
            dto.setCreatedAt(String.valueOf(jobType.getCreatedAt()));
            dto.setUpdatedAt(String.valueOf(jobType.getUpdatedAt()));
            dto.setDescription(jobType.getDescription());
            jobTypeDTOS.add(dto);
        }
        return jobTypeDTOS;
    }

    private void prepareJobTypeSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<JobType> builder) {


        builder.with(jobTypeSpecificationFactory.isEqual("deleted", false));
        if (org.apache.commons.lang.StringUtils.isNotBlank(listRequest.getSearchText())) {
            builder.with(jobTypeSpecificationFactory.like("name", listRequest.getSearchText()));
        }
        if(listRequest.getIsActive()!=null){
            builder.with(jobTypeSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(jobTypeSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(jobTypeSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23,59,59)));
        }

    }

}
