package com.octal.fsm.transformer;


import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.entities.*;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.helper.CodeGenerator;
import com.octal.fsm.repositories.*;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public String transformToEntity(JobDTO.Add addJobDTO, Long tenantId, boolean isSuperAdmin) throws CodeException {
        Job job = new Job();
        job.setCustomerId(addJobDTO.getCustomerDetails().getCustomerId());
        job.setCustomerQuickBookId(addJobDTO.getCustomerDetails().getCustomerQuickBookId());
        job.setJobTypeId(addJobDTO.getJobTypeId()); // Check Required
        job.setCustomerTypeId(addJobDTO.getCustomerTypeId());
        job.setServiceLocation(addJobDTO.getServiceLocation());
        job.setServiceLocationLat(addJobDTO.getServiceLocationLat());
        job.setServiceLocationLng(addJobDTO.getServiceLocationLng());
        job.setJobDescription(addJobDTO.getJobDescription());
        List<JobMappingTask> jobMappingTask = new ArrayList<>();
        for (String jobTaskId : addJobDTO.getJobTaskId()) {
            //Boolean jobTaskExist = jobTaskRepository.existsByUuid(jobTaskId);
            Optional<JobTask> jobTaskExist = jobTaskRepository.findByUuid(jobTaskId);
            if (jobTaskExist.isPresent()) {
                if (jobTaskExist.get().getSequence() == 1) {
                    job.setCurrentTaskId(jobTaskId);  // todo rather than passing whole object of status master in this we can pass either value or uuid of the same.
                    job.setJobStatusMaster(jobTaskExist.get().getJobStatusMaster());
                    job.setJobStatus(jobTaskExist.get().getName());
                }
                JobMappingTask task = new JobMappingTask();
                task.setTaskId(jobTaskId);
                task.setTaskName(jobTaskExist.get().getName());
                task.setTaskShowId(codeGenerator.generateTaskId());
                task.setTaskSequence(jobTaskExist.get().getSequence());
                task.setJobTaskStatus(jobTaskExist.get().getJobStatusMaster().getName());
                task.setAssignType(jobTaskExist.get().getAssignedType());
                task.setJob(job);
                jobMappingTask.add(task);
            }
        }
        if(TextUtils.isEmpty(job.getJobStatus())){
            throw new CodeException("job type is not configured properly", ErrorCode.COMMON);
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
        //job.setBudget(addJobDTO.getBudget());
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
                document.setDocumentUrl(awsS3BaseUrl + documentUrl);
                document.setAttachType("JOB");
                document.setAttachTypeId(job.getJobId());
                document.setUploadedByType(addJobDTO.getUploadedByType());
                document.setUploadedByTypeId(addJobDTO.getUploadedByTypeId());
                document.setUploadedByUserName(addJobDTO.getUploadedByUserName());
                documentsList.add(document);

                JobMappingDocuments jobMappingDocuments = new JobMappingDocuments();
                jobMappingDocuments.setJob(job);
                jobMappingDocuments.setDocumentId(document.getUuid());
                documents.add(jobMappingDocuments);
            }
            documentsRepository.saveAll(documentsList);
            job.setJobMappingDocuments(documents);
        }
        job.setFrontOfficeId(addJobDTO.getFrontOfficeId());
        job.setTenantId(tenantId);
        jobRepository.save(job);
        return job.getUuid();
    }

    public String updateJob(JobDTO.Add addJobDTO, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            Optional<Job> byUuidAndDeletedFalse = jobRepository.findByUuidAndTenantIdAndDeletedFalse(addJobDTO.getJobUuiId(),tenantId);
            if (byUuidAndDeletedFalse.isEmpty()) {
                throw new CodeException("Job not found", ErrorCode.COMMON);
            }
            Job job = byUuidAndDeletedFalse.get();

            // Update Job Tags
            if (addJobDTO.getJobTags() != null) {
                // use the existing persistent collection
                List<JobMappingTags> existingTags = job.getJobMappingTags();
                existingTags.clear();
                for (String jobTagId : addJobDTO.getJobTags()) {
                    Boolean jobTagExist = jobTagRepository.existsByUuid(jobTagId);
                    if (jobTagExist) {
                        JobMappingTags tag = new JobMappingTags();
                        tag.setTagId(jobTagId);
                        tag.setJob(job);      // set back-reference
                        existingTags.add(tag); // add to existing collection
                    }
                }
            }

            if (addJobDTO.getAdditionalNotes() != null)
                job.setAdditionalNotes(addJobDTO.getAdditionalNotes());

            if (addJobDTO.getJobDescription() != null)
                job.setJobDescription(addJobDTO.getJobDescription());

            if (addJobDTO.getServiceLocation() != null)
                job.setServiceLocation(addJobDTO.getServiceLocation());

            if (addJobDTO.getServiceLocationLat() != null)
                job.setServiceLocationLat(addJobDTO.getServiceLocationLat());

            if (addJobDTO.getServiceLocationLng() != null)
                job.setServiceLocationLng(addJobDTO.getServiceLocationLng());

            if (addJobDTO.getBudget() != null)
                job.setBudget(addJobDTO.getBudget());
            try {
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
            jobRepository.save(job);
            return job.getUuid();
        } catch (Exception e) {
            throw new CodeException("Failed to update job", ErrorCode.COMMON);
        }
    }


}
