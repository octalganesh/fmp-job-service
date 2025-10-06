package com.octal.fsm.service.impl;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.Job;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobRepository;
import com.octal.fsm.service.JobService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.transformer.JobTransformer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobTransformer jobTransformer;

    @Autowired
    private SpecificationFactory<Job> jobSpecificationFactory;

    @Override
    public String addJob(JobDTO.Add addJobDTO) throws CodeException {
        try {
            Job job = jobTransformer.transformToEntity(addJobDTO);
            Job savedJob = jobRepository.save(job);
            return String.valueOf(savedJob.getRecordId()); // Using getRecordId() instead of getId()
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public JobDTO.Detail updateJob(JobDTO.Update updateJobDTO) throws CodeException {
        try {
            Optional<Job> existingJobOpt = jobRepository.findByUuidAndDeletedFalse(updateJobDTO.getId());
            if (existingJobOpt.isEmpty()) {
                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
            }

            Job existingJob = existingJobOpt.get();
            Job updatedJob = jobTransformer.updateEntityFromDTO(updateJobDTO, existingJob);
            Job savedJob = jobRepository.save(updatedJob);

            return jobTransformer.transformToDetailDTO(savedJob);
        } catch (CodeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public Boolean deleteJob(String id) throws CodeException {
        try {
            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(id);
            if (jobOpt.isEmpty()) {
                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
            }

            Job job = jobOpt.get();
            job.setDeleted(true);
            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);

            return true;
        } catch (CodeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public JobDTO.Detail getJobById(String id) throws CodeException {
        try {
            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(id);
            if (jobOpt.isEmpty()) {
                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
            }

            return jobTransformer.transformToDetailDTO(jobOpt.get());
        } catch (CodeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public Boolean changeJobStatus(String id, String status) throws CodeException {
        try {
            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(id);
            if (jobOpt.isEmpty()) {
                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
            }

            Job job = jobOpt.get();
            job.setJobStatus(status);
            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);

            return true;
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public PageItem<JobDTO.List> getAllJobs(PageRequest.List listRequest) {
        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
        prepareJobSearchFilter(listRequest, builder);

        String sortDirection = listRequest.getAsc() ? "ASC" : "DESC";
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), listRequest.getShortingField());
        Pageable pageRequest = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), sort);

        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageRequest);
        List<JobDTO.List> responseList = jobTransformer.transformToListDTO(pagedResult.getContent());

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList,
                listRequest.getPageNumber(), listRequest.getPageSize());
    }

    @Override
    public PageItem<JobDTO.List> searchJobs(String searchTerm, PageRequest.List listRequest) {
        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
        prepareJobSearchFilter(listRequest, builder);

        if (StringUtils.isNotBlank(searchTerm)) {
            builder.with(jobSpecificationFactory.like("jobSummary", searchTerm));
        }

        String sortDirection = listRequest.getAsc() ? "ASC" : "DESC";
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), listRequest.getShortingField());
        Pageable pageRequest = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), sort);

        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageRequest);
        List<JobDTO.List> responseList = jobTransformer.transformToListDTO(pagedResult.getContent());

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList,
                listRequest.getPageNumber(), listRequest.getPageSize());
    }

    @Override
    public PageItem<JobDTO.List> getJobsByStatus(String status, PageRequest.List listRequest) {
        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
        prepareJobSearchFilter(listRequest, builder);
        builder.with(jobSpecificationFactory.isEqual("jobStatus", status));

        String sortDirection = listRequest.getAsc() ? "ASC" : "DESC";
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), listRequest.getShortingField());
        Pageable pageRequest = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), sort);

        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageRequest);
        List<JobDTO.List> responseList = jobTransformer.transformToListDTO(pagedResult.getContent());

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList,
                listRequest.getPageNumber(), listRequest.getPageSize());
    }

    @Override
    public PageItem<JobDTO.List> getJobsByPriority(String priority, PageRequest.List listRequest) {
        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
        prepareJobSearchFilter(listRequest, builder);
        builder.with(jobSpecificationFactory.isEqual("priority", priority));

        String sortDirection = listRequest.getAsc() ? "ASC" : "DESC";
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), listRequest.getShortingField());
        Pageable pageRequest = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), sort);

        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageRequest);
        List<JobDTO.List> responseList = jobTransformer.transformToListDTO(pagedResult.getContent());

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList,
                listRequest.getPageNumber(), listRequest.getPageSize());
    }

    @Override
    public JobDTO.Detail assignTechnician(String jobId, String technicianId) throws CodeException {
        try {
            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(jobId);
            if (jobOpt.isEmpty()) {
                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
            }

            Job job = jobOpt.get();
           // job.setAssignedTechnician(jobTransformer.getTechnicianById(technicianId));
            job.setAssignedDateTime(LocalDateTime.now());
            job.setJobStatus("ASSIGNED");
            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
            job.setUpdatedAt(LocalDateTime.now());

            Job savedJob = jobRepository.save(job);
            return jobTransformer.transformToDetailDTO(savedJob);
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public JobDTO.Detail updateJobProgress(String jobId, String summary, String status) throws CodeException {
        try {
            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(jobId);
            if (jobOpt.isEmpty()) {
                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
            }

            Job job = jobOpt.get();
            job.setJobSummary(summary);
            job.setJobStatus(status);
            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
            job.setUpdatedAt(LocalDateTime.now());

            Job savedJob = jobRepository.save(job);
            return jobTransformer.transformToDetailDTO(savedJob);
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public JobDTO.Detail completeJob(String jobId, String summary) throws CodeException {
        try {
            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(jobId);
            if (jobOpt.isEmpty()) {
                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
            }

            Job job = jobOpt.get();
            job.setJobSummary(summary);
            job.setJobStatus("COMPLETED");
            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
            job.setUpdatedAt(LocalDateTime.now());

            Job savedJob = jobRepository.save(job);
            return jobTransformer.transformToDetailDTO(savedJob);
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    private void prepareJobSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<Job> builder) {
        // Always filter out deleted records
        builder.with(jobSpecificationFactory.isEqual("deleted", false));

        // Search by text if provided
        if (StringUtils.isNotBlank(listRequest.getSearchText())) {
            builder.with(jobSpecificationFactory.like("jobSummary", listRequest.getSearchText()));
        }

        // Filter by date range if provided
        if (listRequest.getStartDate() != null) {
            builder.with(jobSpecificationFactory.isGreaterThanOrEquals("createdAt",
                    listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(jobSpecificationFactory.isLessThanOrEquals("createdAt",
                    listRequest.getEndDate().plusDays(1).atStartOfDay()));
        }
    }
}
