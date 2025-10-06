package com.octal.fsm.service.impl;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobRepository;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.transformer.JobTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobTransformer jobTransformer;

    @Mock
    private SpecificationFactory<Job> jobSpecificationFactory;

    @InjectMocks
    private JobServiceImpl jobService;

    private Job testJob;
    private JobDTO.Add addJobDTO;
    private JobDTO.Update updateJobDTO;
    private JobDTO.Detail jobDetailDTO;
    private JobDTO.List jobListDTO;

    @BeforeEach
    void setUp() {
        testJob = new Job();
        testJob.setRecordId(1L);
        testJob.setUuid("test-uuid");
        testJob.setJobSummary("Test Job");
        testJob.setJobStatus("NEW");
        testJob.setPriority("HIGH");
        testJob.setDeleted(false);
        testJob.setCreatedAt(LocalDateTime.now());
        testJob.setUpdatedAt(LocalDateTime.now());

        addJobDTO = new JobDTO.Add();
        addJobDTO.setJobSummary("Test Job");
        addJobDTO.setJobTypeId("1");
        addJobDTO.setPriority("HIGH");
        addJobDTO.setJobStatus("SCHEDULED");
        addJobDTO.setActive(true);

        updateJobDTO = new JobDTO.Update();
        updateJobDTO.setId("test-uuid");
        updateJobDTO.setJobSummary("Updated Job");
        updateJobDTO.setPriority("MEDIUM");

        jobDetailDTO = new JobDTO.Detail();
        jobDetailDTO.setId("test-uuid");
        jobDetailDTO.setJobSummary("Test Job");
        jobDetailDTO.setJobStatus("NEW");

        jobListDTO = new JobDTO.List();
        jobListDTO.setId("test-uuid");
        jobListDTO.setJobSummary("Test Job");
        jobListDTO.setJobStatus("NEW");
    }

    @Test
    void addJob_Success() throws CodeException {
        // Arrange
        when(jobTransformer.transformToEntity(addJobDTO)).thenReturn(testJob);
        when(jobRepository.save(testJob)).thenReturn(testJob);

        // Act
        String result = jobService.addJob(addJobDTO);

        // Assert
        assertThat(result).isEqualTo("1");
        verify(jobTransformer).transformToEntity(addJobDTO);
        verify(jobRepository).save(testJob);
    }

    @Test
    void addJob_ThrowsException_WhenRepositoryFails() {
        // Arrange
        when(jobTransformer.transformToEntity(addJobDTO)).thenReturn(testJob);
        when(jobRepository.save(testJob)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatThrownBy(() -> jobService.addJob(addJobDTO))
                .isInstanceOf(CodeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXCEPTION_OCCUR);
    }

    @Test
    void updateJob_Success() throws CodeException {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.of(testJob));
        when(jobTransformer.updateEntityFromDTO(updateJobDTO, testJob)).thenReturn(testJob);
        when(jobRepository.save(testJob)).thenReturn(testJob);
        when(jobTransformer.transformToDetailDTO(testJob)).thenReturn(jobDetailDTO);

        // Act
        JobDTO.Detail result = jobService.updateJob(updateJobDTO);

        // Assert
        assertThat(result).isEqualTo(jobDetailDTO);
        verify(jobRepository).findByUuidAndDeletedFalse("test-uuid");
        verify(jobTransformer).updateEntityFromDTO(updateJobDTO, testJob);
        verify(jobRepository).save(testJob);
        verify(jobTransformer).transformToDetailDTO(testJob);
    }

    @Test
    void updateJob_ThrowsException_WhenJobNotFound() {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobService.updateJob(updateJobDTO))
                .isInstanceOf(CodeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECORD_NOT_FOUND);
    }

    @Test
    void deleteJob_Success() throws CodeException {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.of(testJob));
        when(jobRepository.save(testJob)).thenReturn(testJob);

        // Act
        Boolean result = jobService.deleteJob("test-uuid");

        // Assert
        assertThat(result).isTrue();
        assertThat(testJob.isDeleted()).isTrue();
        verify(jobRepository).findByUuidAndDeletedFalse("test-uuid");
        verify(jobRepository).save(testJob);
    }

    @Test
    void deleteJob_ThrowsException_WhenJobNotFound() {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobService.deleteJob("test-uuid"))
                .isInstanceOf(CodeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECORD_NOT_FOUND);
    }

    @Test
    void getJobById_Success() throws CodeException {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.of(testJob));
        when(jobTransformer.transformToDetailDTO(testJob)).thenReturn(jobDetailDTO);

        // Act
        JobDTO.Detail result = jobService.getJobById("test-uuid");

        // Assert
        assertThat(result).isEqualTo(jobDetailDTO);
        verify(jobRepository).findByUuidAndDeletedFalse("test-uuid");
        verify(jobTransformer).transformToDetailDTO(testJob);
    }

    @Test
    void getJobById_ThrowsException_WhenJobNotFound() {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobService.getJobById("test-uuid"))
                .isInstanceOf(CodeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECORD_NOT_FOUND);
    }

    @Test
    void changeJobStatus_Success() throws CodeException {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.of(testJob));
        when(jobRepository.save(testJob)).thenReturn(testJob);

        // Act
        Boolean result = jobService.changeJobStatus("test-uuid", "COMPLETED");

        // Assert
        assertThat(result).isTrue();
        assertThat(testJob.getJobStatus()).isEqualTo("COMPLETED");
        verify(jobRepository).findByUuidAndDeletedFalse("test-uuid");
        verify(jobRepository).save(testJob);
    }

    @Test
    @org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
    void getAllJobs_Success() {
        // Arrange
        PageRequest.List listRequest = new PageRequest.List();
        listRequest.setPageNumber(0);
        listRequest.setPageSize(10);
        listRequest.setShortingField("createdAt");
        listRequest.setAsc(false);

        List<Job> jobs = Arrays.asList(testJob);
        Page<Job> jobPage = new PageImpl<>(jobs);
        List<JobDTO.List> jobListDTOs = Arrays.asList(jobListDTO);

        // Mock the specification factory
        @SuppressWarnings("unchecked")
        Specification<Job> mockSpec = mock(Specification.class);
        when(jobSpecificationFactory.isEqual(anyString(), any())).thenReturn(mockSpec);
        when(jobSpecificationFactory.like(anyString(), anyString())).thenReturn(mockSpec);
        when(jobSpecificationFactory.isGreaterThanOrEquals(anyString(), any())).thenReturn(mockSpec);
        when(jobSpecificationFactory.isLessThanOrEquals(anyString(), any())).thenReturn(mockSpec);

        when(jobRepository.findAll(ArgumentMatchers.<Specification<Job>>any(), any(Pageable.class))).thenReturn(jobPage);
        when(jobTransformer.transformToListDTO(jobs)).thenReturn(jobListDTOs);

        // Act
        PageItem<JobDTO.List> result = jobService.getAllJobs(listRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0)).isEqualTo(jobListDTO);
        verify(jobRepository).findAll(ArgumentMatchers.<Specification<Job>>any(), any(Pageable.class));
        verify(jobTransformer).transformToListDTO(anyList());
    }

    @Test
    @org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
    void searchJobs_Success() {
        // Arrange
        PageRequest.List listRequest = new PageRequest.List();
        listRequest.setPageNumber(0);
        listRequest.setPageSize(10);
        listRequest.setShortingField("createdAt");
        listRequest.setAsc(false);

        String searchTerm = "test";
        List<Job> jobs = Arrays.asList(testJob);
        Page<Job> jobPage = new PageImpl<>(jobs);
        List<JobDTO.List> jobListDTOs = Arrays.asList(jobListDTO);

        // Mock the specification factory
        @SuppressWarnings("unchecked")
        Specification<Job> mockSpec = mock(Specification.class);
        when(jobSpecificationFactory.isEqual(anyString(), any())).thenReturn(mockSpec);
        when(jobSpecificationFactory.like(anyString(), anyString())).thenReturn(mockSpec);
        when(jobSpecificationFactory.isGreaterThanOrEquals(anyString(), any())).thenReturn(mockSpec);
        when(jobSpecificationFactory.isLessThanOrEquals(anyString(), any())).thenReturn(mockSpec);

        when(jobRepository.findAll(ArgumentMatchers.<Specification<Job>>any(), any(Pageable.class))).thenReturn(jobPage);
        when(jobTransformer.transformToListDTO(jobs)).thenReturn(jobListDTOs);

        // Act
        PageItem<JobDTO.List> result = jobService.searchJobs(searchTerm, listRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        verify(jobRepository).findAll(ArgumentMatchers.<Specification<Job>>any(), any(Pageable.class));
        verify(jobTransformer).transformToListDTO(jobs);
    }

    @Test
    void assignTechnician_Success() throws CodeException {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.of(testJob));
        when(jobRepository.save(testJob)).thenReturn(testJob);
        when(jobTransformer.transformToDetailDTO(testJob)).thenReturn(jobDetailDTO);

        // Act
        JobDTO.Detail result = jobService.assignTechnician("test-uuid", "tech-1");

        // Assert
        assertThat(result).isEqualTo(jobDetailDTO);
        assertThat(testJob.getJobStatus()).isEqualTo("ASSIGNED");
        assertThat(testJob.getAssignedDateTime()).isNotNull();
        verify(jobRepository).findByUuidAndDeletedFalse("test-uuid");
        verify(jobRepository).save(testJob);
        verify(jobTransformer).transformToDetailDTO(testJob);
    }

    @Test
    void completeJob_Success() throws CodeException {
        // Arrange
        when(jobRepository.findByUuidAndDeletedFalse("test-uuid")).thenReturn(Optional.of(testJob));
        when(jobRepository.save(testJob)).thenReturn(testJob);
        when(jobTransformer.transformToDetailDTO(testJob)).thenReturn(jobDetailDTO);

        // Act
        JobDTO.Detail result = jobService.completeJob("test-uuid", "Job completed successfully");

        // Assert
        assertThat(result).isEqualTo(jobDetailDTO);
        assertThat(testJob.getJobStatus()).isEqualTo("COMPLETED");
        assertThat(testJob.getJobSummary()).isEqualTo("Job completed successfully");
        verify(jobRepository).findByUuidAndDeletedFalse("test-uuid");
        verify(jobRepository).save(testJob);
        verify(jobTransformer).transformToDetailDTO(testJob);
    }
}