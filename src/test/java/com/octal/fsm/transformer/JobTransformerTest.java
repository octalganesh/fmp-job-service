package com.octal.fsm.transformer;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobTag;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.repositories.JobTagRepository;
import com.octal.fsm.repositories.JobTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobTransformerTest {

    @Mock
    private JobTypeRepository jobTypeRepository;

    @Mock
    private JobTagRepository jobTagRepository;

    @InjectMocks
    private JobTransformer jobTransformer;

    private JobDTO.Add addJobDTO;
    private JobDTO.Update updateJobDTO;
    private Job job;
    private JobType jobType;
    private JobTag jobTag;

    @BeforeEach
    void setUp() {
        jobType = new JobType();
        jobType.setRecordId(1L);
        jobType.setName("Test Job Type");
        jobType.setDescription("Test Description");

        jobTag = new JobTag();
        jobTag.setRecordId(1L);
        jobTag.setName("Test Tag");

        addJobDTO = new JobDTO.Add();
        addJobDTO.setJobSummary("Test Job Summary");
        addJobDTO.setJobTypeId("1");
        addJobDTO.setPriority("HIGH");
        addJobDTO.setEstimatedCost(100.0);
        addJobDTO.setJobTimeDuration("2 hours");
        addJobDTO.setJobStatus("SCHEDULED");
        addJobDTO.setActive(true);
        addJobDTO.setTagIds(Set.of("1"));
        addJobDTO.setAssignedDateTime(LocalDateTime.now());

        updateJobDTO = new JobDTO.Update();
        updateJobDTO.setId("job-uuid");
        updateJobDTO.setJobSummary("Updated Job Summary");
        updateJobDTO.setPriority("MEDIUM");
        updateJobDTO.setEstimatedCost(150.0);
        updateJobDTO.setJobTimeDuration("3 hours");
        updateJobDTO.setJobStatus("IN_PROGRESS");
        updateJobDTO.setActive(false);

        job = new Job();
        job.setRecordId(1L);
        job.setUuid("job-uuid");
        job.setJobSummary("Test Job Summary");
        job.setJobStatus("NEW");
        job.setPriority("HIGH");
        job.setEstimatedCost(100.0);
        job.setJobTimeDuration("2 hours");
        job.setActive(true);
        job.setDeleted(false);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        job.setJobType(jobType);
        job.setTags(Set.of(jobTag));
    }

    @Test
    void transformToEntity_Success() {
        // Arrange
        when(jobTypeRepository.findById(1L)).thenReturn(Optional.of(jobType));
        when(jobTagRepository.findById(1L)).thenReturn(Optional.of(jobTag));

        // Act
        Job result = jobTransformer.transformToEntity(addJobDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getJobSummary()).isEqualTo("Test Job Summary");
        assertThat(result.getPriority()).isEqualTo("HIGH");
        assertThat(result.getEstimatedCost()).isEqualTo(100.0);
        assertThat(result.getJobTimeDuration()).isEqualTo("2 hours");
        assertThat(result.getJobStatus()).isEqualTo("SCHEDULED");
        assertThat(result.getActive()).isTrue();
        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getJobType()).isEqualTo(jobType);
        assertThat(result.getTags()).hasSize(1);
        assertThat(result.getTags()).contains(jobTag);
    }

    @Test
    void transformToEntity_WithNullJobTypeId() {
        // Arrange
        addJobDTO.setJobTypeId(null);

        // Act
        Job result = jobTransformer.transformToEntity(addJobDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getJobType()).isNull();
    }

    @Test
    void transformToEntity_WithEmptyTagIds() {
        // Arrange
        addJobDTO.setTagIds(Collections.emptySet());

        // Act
        Job result = jobTransformer.transformToEntity(addJobDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTags()).isEmpty();
    }

    @Test
    void updateEntityFromDTO_Success() {
        // Act
        Job result = jobTransformer.updateEntityFromDTO(updateJobDTO, job);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getJobSummary()).isEqualTo("Updated Job Summary");
        assertThat(result.getPriority()).isEqualTo("MEDIUM");
        assertThat(result.getEstimatedCost()).isEqualTo(150.0);
        assertThat(result.getJobTimeDuration()).isEqualTo("3 hours");
        assertThat(result.getJobStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.getActive()).isFalse();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateEntityFromDTO_WithNullValues() {
        // Arrange
        JobDTO.Update nullUpdateDTO = new JobDTO.Update();
        nullUpdateDTO.setId("job-uuid");
        String originalSummary = job.getJobSummary();
        String originalPriority = job.getPriority();

        // Act
        Job result = jobTransformer.updateEntityFromDTO(nullUpdateDTO, job);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getJobSummary()).isEqualTo(originalSummary);
        assertThat(result.getPriority()).isEqualTo(originalPriority);
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void transformToDetailDTO_Success() {
        // Act
        JobDTO.Detail result = jobTransformer.transformToDetailDTO(job);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getJobSummary()).isEqualTo("Test Job Summary");
        assertThat(result.getJobStatus()).isEqualTo("NEW");
        assertThat(result.getPriority()).isEqualTo("HIGH");
        assertThat(result.getEstimatedCost()).isEqualTo(100.0);
        assertThat(result.getJobTimeDuration()).isEqualTo("2 hours");
        assertThat(result.getActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void transformToListDTO_Success() {
        // Arrange
        List<Job> jobs = Arrays.asList(job);

        // Act
        List<JobDTO.List> result = jobTransformer.transformToListDTO(jobs);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        JobDTO.List listDTO = result.get(0);
        assertThat(listDTO.getId()).isEqualTo("1");
        assertThat(listDTO.getJobSummary()).isEqualTo("Test Job Summary");
        assertThat(listDTO.getJobStatus()).isEqualTo("NEW");
        assertThat(listDTO.getPriority()).isEqualTo("HIGH");
        assertThat(listDTO.getEstimatedCost()).isEqualTo(100.0);
        assertThat(listDTO.getActive()).isTrue();
        assertThat(listDTO.getTags()).containsExactly("Test Tag");
        assertThat(listDTO.getJobTypeId()).isEqualTo("1");
        assertThat(listDTO.getJobTypeName()).isEqualTo("Test Job Type");
    }

    @Test
    void jobToListDTO_StaticFunction_Success() {
        // Act
        JobDTO.List result = JobTransformer.jobToListDTO.apply(job);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getJobSummary()).isEqualTo("Test Job Summary");
        assertThat(result.getJobStatus()).isEqualTo("NEW");
        assertThat(result.getPriority()).isEqualTo("HIGH");
        assertThat(result.getEstimatedCost()).isEqualTo(100.0);
        assertThat(result.getActive()).isTrue();
        assertThat(result.getTags()).containsExactly("Test Tag");
        assertThat(result.getJobTypeId()).isEqualTo("1");
        assertThat(result.getJobTypeName()).isEqualTo("Test Job Type");
    }
}