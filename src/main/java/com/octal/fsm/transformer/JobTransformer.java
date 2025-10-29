package com.octal.fsm.transformer;


import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.CustomerDTO;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.entities.*;
import com.octal.fsm.entities.enums.Gender;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.helper.CodeGenerator;
import com.octal.fsm.repositories.*;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.Objects;

@Component
public class JobTransformer {

    @Autowired
    private JobTypeRepository jobTypeRepository;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobTaskRepository jobTaskRepository;

    @Autowired
    private JobTagRepository jobTagRepository;

    @Autowired
    private DocumentsRepository documentsRepository;
    @Autowired
    private CodeGenerator codeGenerator;
    @Value("${aws.base-url}")
    private String awsS3BaseUrl;

    public String transformToEntity(JobDTO.Add addJobDTO) throws CodeException {
        Job job = new Job();
        job.setCustomerId(addJobDTO.getCustomerDetails().getCustomerId());
        job.setCustomerQuickBookId(addJobDTO.getCustomerDetails().getCustomerQuickBookId());
        job.setJobTypeId(addJobDTO.getJobTypeId()); // Check Required
        job.setCustomerTypeId(addJobDTO.getCustomerTypeId());
        job.setServiceLocation(addJobDTO.getServiceLocation());
        job.setServiceLocationLat(addJobDTO.getServiceLocationLat());
        job.setServiceLocationLng(addJobDTO.getServiceLocationLng());
        job.setJobStatus(addJobDTO.getJobStatus());
        job.setJobDescription(addJobDTO.getJobDescription());
        List<JobMappingTask> jobMappingTask = new ArrayList<>();
        for (String jobTaskId : addJobDTO.getJobTaskId()) {
            Boolean jobTaskExist = jobTaskRepository.existsByUuid(jobTaskId);
            if (jobTaskExist) {
                JobMappingTask task = new JobMappingTask();
                task.setTaskId(jobTaskId);
                task.setTaskShowId(codeGenerator.generateTaskId());
                task.setJob(job);
                jobMappingTask.add(task);
            }
        }
        job.setJobMappingTasks(jobMappingTask);
        try {
            if (!TextUtils.isEmpty(addJobDTO.getLeadReceivedDate())) {
                LocalDate leadReceivedDate = LocalDate.parse(addJobDTO.getLeadReceivedDate());
                job.setLeadReceivedDate(leadReceivedDate);
            }
            if (!TextUtils.isEmpty(addJobDTO.getJobStartDate())) {
                LocalDate jobStartDate = LocalDate.parse(addJobDTO.getJobStartDate());
                job.setJobStartDate(jobStartDate);
            }
            if (!TextUtils.isEmpty(addJobDTO.getJobEndDate())) {
                LocalDate jobEndDate = LocalDate.parse(addJobDTO.getJobEndDate());
                job.setJobEndDate(jobEndDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        job.setLeadSourceId(addJobDTO.getLeadSourceId());
        job.setBudget(addJobDTO.getBudget());
        // Tags
        List<JobMappingTags> jobMappingTags = new ArrayList<>();
        for (String jobTagId : addJobDTO.getJobTags()) {
            Boolean jobTagExist = jobTagRepository.existsByUuid(jobTagId);
            if (jobTagExist) {
                JobMappingTags tag = new JobMappingTags();
                tag.setTagId(jobTagId);
                tag.setJob(job);
                jobMappingTags.add(tag);
            }
        }
        job.setJobMappingTags(jobMappingTags);
        job.setAdditionalNotes(addJobDTO.getAdditionalNotes());
        job.setJobId(codeGenerator.getJobId());
        // Documents
        List<Documents> documentsList = new ArrayList<>();
        List<JobMappingDocuments> documents = new ArrayList<>();
        if (addJobDTO.getDocuments() != null && !addJobDTO.getDocuments().isEmpty()) {
            for (String documentUrl : addJobDTO.getDocuments()) {
                Documents document = new Documents();
                document.setFileName(TextUtils.getFileNameFromFileUrl(documentUrl));
                document.setFileType(TextUtils.getFileTypeFromFileUrl(documentUrl));
                document.setDocumentUrl(awsS3BaseUrl+documentUrl);
                document.setAttachType("JOB");
                document.setAttachTypeId(job.getJobId());
                documentsList.add(document);

                JobMappingDocuments jobMappingDocuments = new JobMappingDocuments();
                jobMappingDocuments.setJob(job);
                jobMappingDocuments.setDocumentId(document.getUuid());
                documents.add(jobMappingDocuments);
            }
            documentsRepository.saveAll(documentsList);
            job.setJobMappingDocuments(documents);
        }
        jobRepository.save(job);
        return job.getUuid();
    }
}
