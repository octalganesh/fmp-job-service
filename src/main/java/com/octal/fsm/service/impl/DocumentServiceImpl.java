package com.octal.fsm.service.impl;

import com.octal.fsm.dto.DocumentDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.*;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.repositories.DocumentsRepository;
import com.octal.fsm.repositories.JobMappingTaskRepository;
import com.octal.fsm.repositories.JobRepository;
import com.octal.fsm.repositories.JobTaskMappingTechnicianRepository;
import com.octal.fsm.service.DocumentService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {


    @Autowired
    private DocumentsRepository documentsRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobMappingTaskRepository jobMappingTaskRepository;

    @Autowired
    private JobTaskMappingTechnicianRepository jobTaskMappingTechnicianRepository;

    @Autowired
    private SpecificationFactory<Documents> documentsSpecificationFactory;
    @Value("${aws.base-url}")
    private String awsS3BaseUrl;

    @Override
    public void uploadDocument(DocumentDTO.Add addJobDTO) throws CodeException {
        if (addJobDTO.getAttachType().equalsIgnoreCase("JOB")) {
            Boolean jobExits = jobRepository.existsByUuidAndDeletedFalse(addJobDTO.getAttachTypeId());
            if (!jobExits)
                throw new CodeException("Job not found", ErrorCode.COMMON);
        } else if (addJobDTO.getAttachType().equalsIgnoreCase("JOB_TASK")) {
            Optional<JobTaskMappingTechnician>jobTaskMappingTechnician=jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(addJobDTO.getAttachTypeId());
            if(jobTaskMappingTechnician.isEmpty())
                throw new CodeException("Job Task not found", ErrorCode.COMMON);
            Boolean jobTaskExists = jobMappingTaskRepository.existsByUuidAndDeletedFalse(jobTaskMappingTechnician.get().getJobTaskMappingId());
            if (!jobTaskExists)
                throw new CodeException("Job Task not found", ErrorCode.COMMON);
            addJobDTO.setAttachTypeId(jobTaskMappingTechnician.get().getJobTaskMappingId());
        } else {
            throw new CodeException("Invalid attach type. Allowed values are JOB or JOB_TASK", ErrorCode.COMMON);
        }
        Documents documents = new Documents();
        documents.setFileName(addJobDTO.getFileName());
        documents.setDocumentUrl(addJobDTO.getDocumentUrl());
        if(addJobDTO.getThumbnail()!=null && addJobDTO.getThumbnail().isEmpty())
            documents.setThumbnail(addJobDTO.getThumbnail());
        documents.setFileType(addJobDTO.getFileType());
        documents.setDocumentTypeId(addJobDTO.getDocumentTypeId());
        documents.setAttachType(addJobDTO.getAttachType());
        documents.setAttachTypeId(addJobDTO.getAttachTypeId());
        documents.setUploadedByType(addJobDTO.getUploadedBType());
        documents.setUploadedByTypeId(addJobDTO.getUploadedBTypeId());
        documents.setUploadedByUserName(addJobDTO.getUploadByUserName());
        documentsRepository.save(documents);
    }

    @Override
    public PageItem<DocumentDTO.ListResponse> getListOfDocument(String type, String typeId, String uploadedByType, String uploadByTypeId, String fileType, int page, int size, String sortBy, Boolean order, String loggedInUserEmail) throws CodeException {
        Job job = null;
        if (type.equalsIgnoreCase("JOB")) {
            Optional<Job> jobExits = jobRepository.findByUuidAndDeletedFalse(typeId);
            if (jobExits.isEmpty())
                throw new CodeException("Job not found", ErrorCode.COMMON);
            job = jobExits.get();
        } else if (type.equalsIgnoreCase("JOB_TASK")) {
            Boolean jobTaskExists = jobMappingTaskRepository.existsByUuidAndDeletedFalse(typeId);
            if (!jobTaskExists)
                throw new CodeException("Job Task not found", ErrorCode.COMMON);
        } else {
            throw new CodeException("Invalid attach type. Allowed values are JOB or JOB_TASK", ErrorCode.COMMON);
        }
        GenericSpecificationsBuilder<Documents> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(order)) {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).descending());
        }
        builder.with(documentsSpecificationFactory.isEqual("deleted", false));
        if(type.equalsIgnoreCase("JOB")){
//            builder.with(documentsSpecificationFactory.isEqual("type", type));
            Set<String> fieldValues = job.getJobMappingTasks()
                    .stream()
                    .map(JobMappingTask::getUuid) // extract only uuid
                    .collect(Collectors.toSet());
            fieldValues.add(typeId);
            builder.with(documentsSpecificationFactory.fieldIn("attachTypeId", fieldValues));
        }else{
            builder.with(documentsSpecificationFactory.isEqual("attachType", type));
            builder.with(documentsSpecificationFactory.isEqual("attachTypeId", typeId));
        }
        Page<Documents> pagedResult = documentsRepository.findAll(builder.build(), pageable);
        List<DocumentDTO.ListResponse> responseList = new ArrayList<>();
        for (Documents doc : pagedResult.getContent()) {
            DocumentDTO.ListResponse response = new DocumentDTO.ListResponse();
            response.setId(doc.getUuid());
            response.setFileName(doc.getFileName());
            response.setDocumentUrl(doc.getDocumentUrl());
            response.setFileType(doc.getFileType());
            response.setUploadedByType(doc.getUploadedByType());
            response.setUploadedByTypeId(doc.getUploadedByTypeId());
            response.setCreatedAt(doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null);
            responseList.add(response);
        }
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, page,
                size);
    }

    @Override
    public void uploadMultipleDocument(List<DocumentDTO.Add> list) throws CodeException {
        List<Documents>documentsList=new ArrayList<>();
        for(DocumentDTO.Add addJobDTO:list){
            if(TextUtils.isEmpty(addJobDTO.getDocumentUrl()))
                throw new CodeException("document url is required", ErrorCode.BAD_REQUEST);
            if(TextUtils.isEmpty(addJobDTO.getFileName()))
                throw new CodeException("file name is required", ErrorCode.BAD_REQUEST);
            if(TextUtils.isEmpty(addJobDTO.getFileType()))
                throw new CodeException("file type is required", ErrorCode.BAD_REQUEST);
            if(TextUtils.isEmpty(addJobDTO.getAttachTypeId()))
                throw new CodeException("attach type id is required", ErrorCode.BAD_REQUEST);
            if(TextUtils.isEmpty(addJobDTO.getDocumentTypeId()))
                throw new CodeException("document type id is required", ErrorCode.BAD_REQUEST);
            if (addJobDTO.getAttachType().equalsIgnoreCase("JOB")) {
                Boolean jobExits = jobRepository.existsByUuidAndDeletedFalse(addJobDTO.getAttachTypeId());
                if (!jobExits)
                    throw new CodeException("Job not found", ErrorCode.COMMON);
            } else if (addJobDTO.getAttachType().equalsIgnoreCase("JOB_TASK")) {
                Optional<JobTaskMappingTechnician> jobTaskMappingTechnician = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(addJobDTO.getAttachTypeId());
                if (jobTaskMappingTechnician.isEmpty())
                    throw new CodeException("Job Task not found", ErrorCode.COMMON);
                Boolean jobTaskExists = jobMappingTaskRepository.existsByUuidAndDeletedFalse(jobTaskMappingTechnician.get().getJobTaskMappingId());
                if (!jobTaskExists)
                    throw new CodeException("Job Task not found", ErrorCode.COMMON);
                addJobDTO.setAttachTypeId(jobTaskMappingTechnician.get().getJobTaskMappingId());
            } else {
                throw new CodeException("Invalid attach type. Allowed values are JOB or JOB_TASK", ErrorCode.BAD_REQUEST);
            }
            Documents documents = new Documents();
            documents.setFileName(addJobDTO.getFileName());
            documents.setDocumentUrl(awsS3BaseUrl+addJobDTO.getDocumentUrl());
            documents.setFileType(addJobDTO.getFileType());
            if(addJobDTO.getThumbnail()!=null && !addJobDTO.getThumbnail().isEmpty())
                documents.setThumbnail(addJobDTO.getThumbnail());
            documents.setDocumentTypeId(addJobDTO.getDocumentTypeId());
            documents.setAttachType(addJobDTO.getAttachType());
            documents.setAttachTypeId(addJobDTO.getAttachTypeId());
            documents.setUploadedByType(addJobDTO.getUploadedBType());
            documents.setUploadedByTypeId(addJobDTO.getUploadedBTypeId());
            documents.setUploadedByUserName(addJobDTO.getUploadByUserName());
            documentsList.add(documents);
        }
        documentsRepository.saveAll(documentsList);
    }

}
