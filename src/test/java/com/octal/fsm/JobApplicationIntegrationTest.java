package com.octal.fsm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.JobTagDTO;
import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobTag;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.repositories.JobRepository;
import com.octal.fsm.repositories.JobTagRepository;
import com.octal.fsm.repositories.JobTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JobApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobTypeRepository jobTypeRepository;

    @Autowired
    private JobTagRepository jobTagRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private JobType testJobType;
    private JobTag testJobTag;
    private Job testJob;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        jobRepository.deleteAll();
        jobTagRepository.deleteAll();
        jobTypeRepository.deleteAll();

        // Create test data
        testJobType = new JobType();
        testJobType.setName("Integration Test Job Type");
        testJobType.setDescription("Test Description");
        testJobType.setActive(true);
        testJobType.setDeleted(false);
        testJobType.setCreatedAt(LocalDateTime.now());
        testJobType.setUpdatedAt(LocalDateTime.now());
        testJobType = jobTypeRepository.save(testJobType);

        testJobTag = new JobTag();
        testJobTag.setName("Integration Test Tag");
        testJobTag.setActive(true);
        testJobTag.setDeleted(false);
        testJobTag.setCreatedAt(LocalDateTime.now());
        testJobTag.setUpdatedAt(LocalDateTime.now());
        testJobTag = jobTagRepository.save(testJobTag);

        testJob = new Job();
        testJob.setJobSummary("Integration Test Job");
        testJob.setJobStatus("NEW");
        testJob.setPriority("HIGH");
        testJob.setEstimatedCost(100.0);
        testJob.setJobTimeDuration("2 hours");
        testJob.setActive(true);
        testJob.setDeleted(false);
        testJob.setCreatedAt(LocalDateTime.now());
        testJob.setUpdatedAt(LocalDateTime.now());
        testJob.setJobType(testJobType);
        testJob = jobRepository.save(testJob);
    }

    @Test
    void createJob_IntegrationTest() throws Exception {
        // Arrange
        JobDTO.Add addJobDTO = new JobDTO.Add();
        addJobDTO.setJobSummary("New Integration Test Job");
        addJobDTO.setJobTypeId(String.valueOf(testJobType.getRecordId()));
        addJobDTO.setPriority("MEDIUM");
        addJobDTO.setEstimatedCost(200.0);
        addJobDTO.setCustomerId("customer-1");
        addJobDTO.setJobStatus("SCHEDULED");
        addJobDTO.setActive(true);

        // Act & Assert
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Name", "testuser")
                        .content(objectMapper.writeValueAsString(addJobDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job created successfully"))
                .andExpect(jsonPath("$.data").isNotEmpty());

        // Verify in database
        long jobCount = jobRepository.count();
        assertThat(jobCount).isEqualTo(2); // Original test job + new job
    }

    @Test
    void getJobById_IntegrationTest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/jobs/" + testJob.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(String.valueOf(testJob.getRecordId())))
                .andExpect(jsonPath("$.data.jobSummary").value("Integration Test Job"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    void updateJob_IntegrationTest() throws Exception {
        // Arrange
        JobDTO.Update updateJobDTO = new JobDTO.Update();
        updateJobDTO.setId(testJob.getUuid());
        updateJobDTO.setJobSummary("Updated Integration Test Job");
        updateJobDTO.setPriority("LOW");
        updateJobDTO.setEstimatedCost(150.0);

        // Act & Assert
        mockMvc.perform(put("/jobs/" + testJob.getUuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Name", "testuser")
                        .content(objectMapper.writeValueAsString(updateJobDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job updated successfully"))
                .andExpect(jsonPath("$.data.jobSummary").value("Updated Integration Test Job"))
                .andExpect(jsonPath("$.data.priority").value("LOW"));

        // Verify in database
        Job updatedJob = jobRepository.findByUuidAndDeletedFalse(testJob.getUuid()).orElse(null);
        assertThat(updatedJob).isNotNull();
        assertThat(updatedJob.getJobSummary()).isEqualTo("Updated Integration Test Job");
        assertThat(updatedJob.getPriority()).isEqualTo("LOW");
        assertThat(updatedJob.getEstimatedCost()).isEqualTo(150.0);
    }

    @Test
    void deleteJob_IntegrationTest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/jobs/" + testJob.getUuid())
                        .header("X-User-Name", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job deleted successfully"))
                .andExpect(jsonPath("$.data").value(true));

        // Verify in database
        Job deletedJob = jobRepository.findByUuidAndDeletedFalse(testJob.getUuid()).orElse(null);
        assertThat(deletedJob).isNull();

        // Verify soft delete
        Job softDeletedJob = jobRepository.findById(testJob.getRecordId()).orElse(null);
        assertThat(softDeletedJob).isNotNull();
        assertThat(softDeletedJob.isDeleted()).isTrue();
    }

    @Test
    void getAllJobs_IntegrationTest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/jobs/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc")
                        .header("X-User-Name", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(String.valueOf(testJob.getRecordId())))
                .andExpect(jsonPath("$.data.items[0].jobSummary").value("Integration Test Job"));
    }

    @Test
    void searchJobs_IntegrationTest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/jobs/search")
                        .param("searchTerm", "Integration")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc")
                        .header("X-User-Name", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Jobs search completed successfully"))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void changeJobStatus_IntegrationTest() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/jobs/" + testJob.getUuid() + "/status")
                        .param("status", "COMPLETED")
                        .header("X-User-Name", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job status updated successfully"))
                .andExpect(jsonPath("$.data").value(true));

        // Verify in database
        Job updatedJob = jobRepository.findByUuidAndDeletedFalse(testJob.getUuid()).orElse(null);
        assertThat(updatedJob).isNotNull();
        assertThat(updatedJob.getJobStatus()).isEqualTo("COMPLETED");
    }
}