package com.octal.fsm.service.impl;

import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobTaskDTO;
import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.JobTask;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobStatusMasterRepository;
import com.octal.fsm.repositories.JobTypeRepository;
import com.octal.fsm.service.JobTypeService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobTypeServiceImpl implements JobTypeService {

    @Autowired
    private JobTypeRepository jobTypeRepository;
    @Autowired
    private SpecificationFactory<JobType> jobTypeSpecificationFactory;
    @Autowired
    private JobStatusMasterRepository jobStatusMasterRepository;

    @Value("${aws.base-url}")
    private String awsS3BaseUrl;

    @Override
    public String addJobType(JobTypeDTO.Add add, Long tenantId, Boolean isSuperAdmin) throws CodeException {
//        if (TextUtils.isEmpty(add.getName()))
//            throw new CodeException("type name is required", ErrorCode.COMMON);
//        Optional<JobType> optionalJobType = jobTypeRepository.findByUuid(add.getId());
//        if (optionalJobType.isPresent() && !optionalJobType.get().getUuid().equals(add.getId())) {
//            throw new CodeException("jobType is already present!", ErrorCode.RECORD_NOT_FOUND);
//        }
//        JobType newJobTypeRecord = null;
//        if (TextUtils.isEmpty(add.getId())) {
//            newJobTypeRecord = new JobType();
//            newJobTypeRecord.setCreatedAt(LocalDateTime.now());
//            newJobTypeRecord.setUpdatedAt(LocalDateTime.now());
//            if (add.getJobTasks() != null) {
//                newJobTypeRecord.getJobTasks().addAll(add.getJobTasks().stream()
//                        .map(dto -> {
//                            com.octal.fsm.entities.JobTask entity = new com.octal.fsm.entities.JobTask();
//                            entity.setName(dto.getName());
//                            entity.setDescription(dto.getDescription());
//                            return entity;
//                        })
//                        .collect(java.util.stream.Collectors.toList()));
//            }
//        } else {
//            Optional<JobType> jobType = jobTypeRepository.findByUuid(add.getId());
//            if (jobType.isPresent()) {
//                newJobTypeRecord = jobType.get();
//                newJobTypeRecord.setUpdatedAt(LocalDateTime.now());
//                newJobTypeRecord.getJobTasks().clear();
//                if (add.getJobTasks() != null) {
//                    newJobTypeRecord.getJobTasks().addAll(add.getJobTasks().stream()
//                            .map(dto -> {
//                                com.octal.fsm.entities.JobTask entity = new com.octal.fsm.entities.JobTask();
//                                entity.setName(dto.getName());
//                                entity.setDescription(dto.getDescription());
//                                return entity;
//                            })
//                            .collect(java.util.stream.Collectors.toList()));
//                }
//            } else {
//                throw new CodeException("jobType not Found!", ErrorCode.COMMON);
//            }
//        }
//        newJobTypeRecord.setActive(add.getIsActive());
//        newJobTypeRecord.setDeleted(false);
//        newJobTypeRecord.setName(add.getName());
//        newJobTypeRecord.setDescription(add.getDescription());
//        JobType jobType = jobTypeRepository.save
//                (newJobTypeRecord);
//        return jobType.getUuid();
        if (isSuperAdmin)
            tenantId = 1L;

        if (TextUtils.isEmpty(add.getName())) {
            throw new CodeException("Type name is required", ErrorCode.COMMON);
        }

        JobType jobTypeRecord;

// CREATE case
        if (TextUtils.isEmpty(add.getId())) {
            jobTypeRecord = new JobType();
            jobTypeRecord.setCreatedAt(LocalDateTime.now());
            jobTypeRecord.setDeleted(false);
            jobTypeRecord.setTenantId(tenantId);
            if (add.getDocuments() != null && !add.getDocuments().isEmpty()) {
                List<String> finalDocs = add.getDocuments()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(doc -> awsS3BaseUrl + doc)
                        .collect(Collectors.toList());
                jobTypeRecord.getJobTypeDocuments().addAll(finalDocs);
            }
        }
// UPDATE case
        else {
            jobTypeRecord = jobTypeRepository.findByUuid(add.getId())
                    .orElseThrow(() -> new CodeException("JobType not found!", ErrorCode.COMMON));
            jobTypeRecord.setUpdatedAt(LocalDateTime.now());

            if (add.getDocuments() != null && !add.getDocuments().isEmpty()) {
                Set<String> uniqueDocs = add.getDocuments()
                        .stream().filter(Objects::nonNull)
                        .map(doc -> doc.startsWith("https")
                                ? doc : awsS3BaseUrl + doc).collect(Collectors.toCollection(LinkedHashSet::new));
                jobTypeRecord.setJobTypeDocuments(new ArrayList<>(uniqueDocs));
            }
        }

// Update common fields
        jobTypeRecord.setName(add.getName());
        jobTypeRecord.setDescription(add.getDescription());
        jobTypeRecord.setActive(add.getIsActive());
        jobTypeRecord.setUpdatedAt(LocalDateTime.now());

// Handle job tasks update (preserve, update, sequence, and back-reference)
        if (add.getJobTasks() != null) {
            Map<String, JobTask> existingTasks = jobTypeRecord.getJobTasks().stream()
                    .collect(Collectors.toMap(JobTask::getUuid, t -> t));

            List<JobTask> updatedTasks = new ArrayList<>();

            Set<String> incomingIds = add.getJobTasks().stream()
                    .map(JobTaskDTO.Add::getId)
                    .filter(id -> !TextUtils.isEmpty(id))
                    .collect(Collectors.toSet());

            for (JobTaskDTO.Add dto : add.getJobTasks()) {
                JobTask taskEntity;

                // Update existing task if ID matches
                if (!TextUtils.isEmpty(dto.getId()) && existingTasks.containsKey(dto.getId())) {
                    taskEntity = existingTasks.get(dto.getId());
                    taskEntity.setName(dto.getName());
                    taskEntity.setDescription(dto.getDescription());
                    taskEntity.setUpdatedAt(LocalDateTime.now());
                }
                // Otherwise, create new
                else {
                    taskEntity = new JobTask();
                    taskEntity.setName(dto.getName());
                    taskEntity.setDescription(dto.getDescription());
                    taskEntity.setCreatedAt(LocalDateTime.now());
                    taskEntity.setUpdatedAt(LocalDateTime.now());
                }

                taskEntity.setSequence(dto.getSequence());
                taskEntity.setAssignedType(dto.getAssignedType());
                taskEntity.setJobStatusMaster(jobStatusMasterRepository.findByUuid(dto.getStatusMasterId()).
                        orElseThrow(() -> new CodeException("Job Status Master not found!", ErrorCode.COMMON)));
                // Set the back-reference for bidirectional mapping
                taskEntity.setJobType(jobTypeRecord);

                updatedTasks.add(taskEntity);
            }

            // Remove tasks not present in incoming DTO
            jobTypeRecord.getJobTasks().removeIf(
                    t -> t.getUuid() != null && !incomingIds.contains(t.getUuid())
            );

            // Clear/add updated tasks to preserve sequence/order
            jobTypeRecord.getJobTasks().clear();
            jobTypeRecord.getJobTasks().addAll(updatedTasks);
        }

        JobType savedJobType = jobTypeRepository.save(jobTypeRecord);
        return savedJobType.getUuid();

    }

    @Override
    public void addJobTaskByJobTypeId(JobTaskDTO.Add add) throws CodeException {
        if (TextUtils.isEmpty(add.getJobTypeId()))
            throw new CodeException("jobTypeId is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(add.getName()))
            throw new CodeException("task name is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(add.getDescription()))
            throw new CodeException("task description is required", ErrorCode.COMMON);
        Optional<JobType> jobTypeOptional = jobTypeRepository.findByUuid(add.getJobTypeId());
        if (jobTypeOptional.isEmpty())
            throw new CodeException("Job Type Not Found.", ErrorCode.COMMON);
        if (!TextUtils.isEmpty(add.getId())) {
            Optional<JobTask> jobTaskOptional = jobTypeOptional.get().getJobTasks().stream().filter(t -> t.getUuid().equals(add.getId())).findFirst();
            if (jobTaskOptional.isPresent()) {
                jobTaskOptional.get().setName(add.getName());
                jobTaskOptional.get().setDescription(add.getDescription());
                jobTypeRepository.save(jobTypeOptional.get());
            } else {
                throw new CodeException("Job Task Not Found.", ErrorCode.COMMON);
            }
        } else {
            List<JobTask> jobTasks = jobTypeOptional.get().getJobTasks();
            com.octal.fsm.entities.JobTask entity = new com.octal.fsm.entities.JobTask();
            entity.setName(add.getName());
            entity.setDescription(add.getDescription());
            jobTasks.add(entity);
            jobTypeOptional.get().setJobTasks(jobTasks);
            jobTypeRepository.save(jobTypeOptional.get());
        }
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
    public JobTypeDTO.Detail getJobTypeByUuid(String id, Long tenantId, Boolean isSuperAdmin) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
        Optional<JobType> jobTypeOptional = jobTypeRepository.findByUuidAndTenantId(id, tenantId);
        if (jobTypeOptional.isPresent()) {
            JobTypeDTO.Detail jobType = new JobTypeDTO.Detail();
            jobType.setName(jobTypeOptional.get().getName());
            jobType.setId(jobTypeOptional.get().getUuid());
            jobType.setIsActive(jobTypeOptional.get().getActive());
            jobType.setCreatedAt(jobTypeOptional.get().getCreatedAt().toString());
            if(jobTypeOptional.get().getJobTypeDocuments() != null){
                jobType.setDocuments(jobTypeOptional.get().getJobTypeDocuments());
            }
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
    public PageItem<JobTypeDTO.Detail> getAllJobTypes(PageRequest.List listRequest, Long tenantId, Boolean isSuperAdmin) {
        if (isSuperAdmin)
            tenantId = 1L;
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        GenericSpecificationsBuilder<JobType> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }

        builder.with(jobTypeSpecificationFactory.isEqual("tenantId", tenantId));

        prepareJobTypeSearchFilter(listRequest, builder);
        Page<JobType> pagedResult = jobTypeRepository.findAll(builder.build(), pageable);
        List<JobTypeDTO.Detail> responseList = new ArrayList<>();
        for (JobType jobType : pagedResult.getContent()) {
            JobTypeDTO.Detail dto = new JobTypeDTO.Detail();
            dto.setId(jobType.getUuid());
            dto.setName(jobType.getName());
            dto.setIsActive(jobType.getActive());
            dto.setCreatedAt(String.valueOf(jobType.getCreatedAt()));
            dto.setUpdatedAt(String.valueOf(jobType.getUpdatedAt()));
            dto.setDescription(jobType.getDescription());
            if(jobType.getJobTypeDocuments() != null){
                dto.setDocuments(jobType.getJobTypeDocuments());
            }
            dto.setJobTasks(jobType.getJobTasks().stream()
                    .map(entity -> {
                        JobTaskDTO.Detail taskDto = new JobTaskDTO.Detail();
                        taskDto.setId(entity.getUuid());
                        taskDto.setName(entity.getName());
                        taskDto.setDescription(entity.getDescription());
                        taskDto.setAssignedType(entity.getAssignedType());
                        taskDto.setIsActive(entity.getActive());
                        taskDto.setStatusMasterId(entity.getJobStatusMaster().getUuid());
                        taskDto.setSequence(entity.getSequence());
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
        List<JobType> jobTypes = jobTypeRepository.findAll();
        List<JobTypeDTO.Detail> jobTypeDTOS = new ArrayList<>();
        for (JobType jobType : jobTypes) {
            JobTypeDTO.Detail dto = new JobTypeDTO.Detail();
            dto.setId(jobType.getUuid());
            dto.setName(jobType.getName());
            dto.setIsActive(jobType.getActive());
            dto.setCreatedAt(String.valueOf(jobType.getCreatedAt()));
            dto.setUpdatedAt(String.valueOf(jobType.getUpdatedAt()));
            dto.setDescription(jobType.getDescription());
            if(jobType.getJobTypeDocuments() != null){
                dto.setDocuments(jobType.getJobTypeDocuments());
            }
            jobTypeDTOS.add(dto);
        }
        return jobTypeDTOS;
    }

    @Override
    public PageItem<JobTypeDTO.DetailWithoutJobTasks> getAllJobTypesForTechnician(PageRequest.List listRequest, Long tenantId, Boolean isSuperAdmin) {
        if (isSuperAdmin)
            tenantId = 1L;
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        GenericSpecificationsBuilder<JobType> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }

        builder.with(jobTypeSpecificationFactory.isEqual("tenantId", tenantId));

        prepareJobTypeSearchFilter(listRequest, builder);
        Page<JobType> pagedResult = jobTypeRepository.findAll(builder.build(), pageable);
        List<JobTypeDTO.DetailWithoutJobTasks> responseList = new ArrayList<>();
        for (JobType jobType : pagedResult.getContent()) {
            JobTypeDTO.DetailWithoutJobTasks dto = new JobTypeDTO.DetailWithoutJobTasks();
            dto.setId(jobType.getUuid());
            dto.setName(jobType.getName());
            dto.setIsActive(jobType.getActive());
            dto.setCreatedAt(String.valueOf(jobType.getCreatedAt()));
            dto.setUpdatedAt(String.valueOf(jobType.getUpdatedAt()));
            dto.setDescription(jobType.getDescription());
            responseList.add(dto);
        }

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(),
                listRequest.getPageSize());
    }

    private void prepareJobTypeSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<JobType> builder) {
        builder.with(jobTypeSpecificationFactory.isEqual("deleted", false));
        if (org.apache.commons.lang.StringUtils.isNotBlank(listRequest.getSearchText())) {
            builder.with(jobTypeSpecificationFactory.like("name", listRequest.getSearchText()));
        }
        if (listRequest.getIsActive() != null) {
            builder.with(jobTypeSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(jobTypeSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(jobTypeSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }

    }

}
