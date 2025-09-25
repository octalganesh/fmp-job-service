package com.octal.fsm.transformer;


import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.entities.JobTag;
import com.octal.fsm.repositories.JobTypeRepository;
import com.octal.fsm.repositories.JobTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    private JobTagRepository jobTagRepository;

    public Job transformToEntity(JobDTO.Add addJobDTO) {
        Job job = new Job();
        job.setJobSummary(addJobDTO.getJobSummary());
        job.setPriority(addJobDTO.getPriority());
        job.setEstimatedCost(addJobDTO.getEstimatedCost());
        job.setJobTimeDuration(addJobDTO.getJobTimeDuration());
        job.setJobStatus(addJobDTO.getJobStatus());
        job.setActive(addJobDTO.getActive());
        job.setAssignedDateTime(addJobDTO.getAssignedDateTime());

        // Set relationships - Convert String IDs to appropriate types
//        if (addJobDTO.getCustomerId() != null) {
//            Customer customer = customerRepository.findById(Long.valueOf(addJobDTO.getCustomerId())).orElse(null);
//            job.setCustomer(customer);
//        }

        if (addJobDTO.getJobTypeId() != null) {
            JobType jobType = jobTypeRepository.findById(Long.valueOf(addJobDTO.getJobTypeId())).orElse(null);
            job.setJobType(jobType);
        }

//        if (addJobDTO.getAssignedTechnicianId() != null) {
//            Technician technician = technicianRepository.findById(Long.valueOf(addJobDTO.getAssignedTechnicianId())).orElse(null);
//            job.setAssignedTechnician(technician);
//        }

        if (addJobDTO.getTagIds() != null && !addJobDTO.getTagIds().isEmpty()) {
            Set<JobTag> tags = addJobDTO.getTagIds().stream()
                    .map(tagId -> jobTagRepository.findById(Long.valueOf(tagId)).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            job.setTags(tags);
        }

        // Set audit fields - Removed setCreatedBy and setUpdatedBy since they don't exist in AbstractPersistable
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        job.setDeleted(false);

        return job;
    }

    public Job updateEntityFromDTO(JobDTO.Update updateJobDTO, Job existingJob) {
        if (updateJobDTO.getJobSummary() != null) {
            existingJob.setJobSummary(updateJobDTO.getJobSummary());
        }
        if (updateJobDTO.getPriority() != null) {
            existingJob.setPriority(updateJobDTO.getPriority());
        }
        if (updateJobDTO.getEstimatedCost() != null) {
            existingJob.setEstimatedCost(updateJobDTO.getEstimatedCost());
        }
        if (updateJobDTO.getJobTimeDuration() != null) {
            existingJob.setJobTimeDuration(updateJobDTO.getJobTimeDuration());
        }
        if (updateJobDTO.getJobStatus() != null) {
            existingJob.setJobStatus(updateJobDTO.getJobStatus());
        }
        if (updateJobDTO.getActive() != null) {
            existingJob.setActive(updateJobDTO.getActive());
        }
        if (updateJobDTO.getAssignedDateTime() != null) {
            existingJob.setAssignedDateTime(updateJobDTO.getAssignedDateTime());
        }
        if (updateJobDTO.getInvoiceSharedDate() != null) {
            existingJob.setInvoiceSharedDate(updateJobDTO.getInvoiceSharedDate());
        }
        if (updateJobDTO.getInvoiceStatus() != null) {
            existingJob.setInvoiceStatus(updateJobDTO.getInvoiceStatus());
        }
        if (updateJobDTO.getPaymentStatus() != null) {
            existingJob.setPaymentStatus(updateJobDTO.getPaymentStatus());
        }
        if (updateJobDTO.getPaymentReceiveDate() != null) {
            existingJob.setPaymentReceiveDate(updateJobDTO.getPaymentReceiveDate());
        }

        // Update relationships - Convert String IDs to appropriate types
//        if (updateJobDTO.getCustomerId() != null) {
//            Customer customer = customerRepository.findById(Long.valueOf(updateJobDTO.getCustomerId())).orElse(null);
//            existingJob.setCustomer(customer);
//        }

        if (updateJobDTO.getJobTypeId() != null) {
            JobType jobType = jobTypeRepository.findById(Long.valueOf(updateJobDTO.getJobTypeId())).orElse(null);
            existingJob.setJobType(jobType);
        }

//        if (updateJobDTO.getAssignedTechnicianId() != null) {
//            Technician technician = technicianRepository.findById(Long.valueOf(updateJobDTO.getAssignedTechnicianId())).orElse(null);
//            existingJob.setAssignedTechnician(technician);
//        }

        if (updateJobDTO.getTagIds() != null) {
            Set<JobTag> tags = updateJobDTO.getTagIds().stream()
                    .map(tagId -> jobTagRepository.findById(Long.valueOf(tagId)).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            existingJob.setTags(tags);
        }

        // Update audit fields - Removed setUpdatedBy since it doesn't exist in AbstractPersistable
        existingJob.setUpdatedAt(LocalDateTime.now());

        return existingJob;
    }

    public static final Function<Job, JobDTO.List> jobToListDTO = job -> {
        JobDTO.List listDTO = new JobDTO.List();
        listDTO.setId(String.valueOf(job.getRecordId())); // Convert Long to String
        listDTO.setJobSummary(job.getJobSummary());
        listDTO.setJobStatus(job.getJobStatus());
        listDTO.setPriority(job.getPriority());
        listDTO.setEstimatedCost(job.getEstimatedCost());
        listDTO.setAssignedDateTime(job.getAssignedDateTime());
        listDTO.setInvoiceSharedDate(job.getInvoiceSharedDate());
        listDTO.setPaymentReceiveDate(job.getPaymentReceiveDate());
        listDTO.setInvoiceStatus(job.getInvoiceStatus());
        listDTO.setPaymentStatus(job.getPaymentStatus());
        listDTO.setActive(job.getActive());
        listDTO.setCreatedAt(job.getCreatedAt());
        listDTO.setUpdatedAt(job.getUpdatedAt());

        // Set customer info
//        if (job.getCustomer() != null) {
//            listDTO.setCustomerId(String.valueOf(job.getCustomer().getRecordId())); // Convert Long to String
//            listDTO.setCustomerName(job.getCustomer().getName());
//        }

        // Set job type info
        if (job.getJobType() != null) {
            listDTO.setJobTypeId(String.valueOf(job.getJobType().getRecordId())); // Convert Long to String
            listDTO.setJobTypeName(job.getJobType().getName());
        }

        // Set technician info
//        if (job.getAssignedTechnician() != null) {
//            listDTO.setAssignedTechnicianId(String.valueOf(job.getAssignedTechnician().getRecordId())); // Convert Long to String
//            listDTO.setAssignedTechnicianName(job.getAssignedTechnician().getName());
//        }

        // Set tags
        if (job.getTags() != null) {
            Set<String> tagNames = job.getTags().stream()
                    .map(JobTag::getName)
                    .collect(Collectors.toSet());
            listDTO.setTags(tagNames);
        }

        return listDTO;
    };

    public JobDTO.Detail transformToDetailDTO(Job job) {
        JobDTO.Detail detailDTO = new JobDTO.Detail();
        detailDTO.setId(String.valueOf(job.getRecordId())); // Convert Long to String
        detailDTO.setJobSummary(job.getJobSummary());
        detailDTO.setJobStatus(job.getJobStatus());
        detailDTO.setPriority(job.getPriority());
        detailDTO.setEstimatedCost(job.getEstimatedCost());
        detailDTO.setJobTimeDuration(job.getJobTimeDuration());
        detailDTO.setAssignedDateTime(job.getAssignedDateTime());
        detailDTO.setInvoiceSharedDate(job.getInvoiceSharedDate());
        detailDTO.setPaymentReceiveDate(job.getPaymentReceiveDate());
        detailDTO.setInvoiceStatus(job.getInvoiceStatus());
        detailDTO.setPaymentStatus(job.getPaymentStatus());
        detailDTO.setActive(job.getActive());
        detailDTO.setCreatedAt(job.getCreatedAt());
        detailDTO.setUpdatedAt(job.getUpdatedAt());

        // Note: For related entities (customer, jobType, assignedTechnician, tags),
        // you would need to transform them using their respective transformers
        // This is a simplified version - you may need to adjust based on your actual DTO structures

        return detailDTO;
    }

    public List<JobDTO.List> transformToListDTO(List<Job> jobs) {
        return jobs.stream()
                .map(jobToListDTO)
                .collect(Collectors.toList());
    }

//    public Technician getTechnicianById(String technicianId) {
//        return technicianRepository.findById(Long.valueOf(technicianId)).orElse(null);
//    }
}
