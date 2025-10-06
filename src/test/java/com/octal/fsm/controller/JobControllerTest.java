package com.octal.fsm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    private JobDTO.Add addJobDTO;
    private JobDTO.Detail jobDetailDTO;
    private JobDTO.List jobListDTO;
    private PageItem<JobDTO.List> pageItem;

    @BeforeEach
    void setUp() {
        addJobDTO = new JobDTO.Add();
        addJobDTO.setJobSummary("Test Job");
        addJobDTO.setJobTypeId("1");
        addJobDTO.setPriority("HIGH");
        addJobDTO.setCustomerId("customer-1");
        addJobDTO.setJobStatus("SCHEDULED");
        addJobDTO.setActive(true);

        jobDetailDTO = new JobDTO.Detail();
        jobDetailDTO.setId("job-uuid");
        jobDetailDTO.setJobSummary("Test Job");
        jobDetailDTO.setJobStatus("NEW");
        jobDetailDTO.setPriority("HIGH");
        jobDetailDTO.setCreatedAt(LocalDateTime.now());
        jobDetailDTO.setUpdatedAt(LocalDateTime.now());

        jobListDTO = new JobDTO.List();
        jobListDTO.setId("job-uuid");
        jobListDTO.setJobSummary("Test Job");
        jobListDTO.setJobStatus("NEW");
        jobListDTO.setPriority("HIGH");
        jobListDTO.setCreatedAt(LocalDateTime.now());
        jobListDTO.setUpdatedAt(LocalDateTime.now());

        List<JobDTO.List> jobs = Arrays.asList(jobListDTO);
        pageItem = new PageItem<>(1, 1L, jobs, 0, 10);
    }

    @Test
    void createJob_Success() throws Exception {
        // Arrange
        when(jobService.addJob(any(JobDTO.Add.class))).thenReturn("job-uuid");

        // Act & Assert
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.USER_NAME, "testuser")
                        .content(objectMapper.writeValueAsString(addJobDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job created successfully"))
                .andExpect(jsonPath("$.data").value("job-uuid"));

        verify(jobService).addJob(any(JobDTO.Add.class));
    }

    @Test
    void getJobById_Success() throws Exception {
        // Arrange
        when(jobService.getJobById("job-uuid")).thenReturn(jobDetailDTO);

        // Act & Assert
        mockMvc.perform(get("/jobs/job-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value("job-uuid"));

        verify(jobService).getJobById("job-uuid");
    }

    @Test
    void deleteJob_Success() throws Exception {
        // Arrange
        when(jobService.deleteJob("job-uuid")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/jobs/job-uuid")
                        .header(CommonConstants.USER_NAME, "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job deleted successfully"))
                .andExpect(jsonPath("$.data").value(true));

        verify(jobService).deleteJob("job-uuid");
    }

    @Test
    void getAllJobs_Success() throws Exception {
        // Arrange
        when(jobService.getAllJobs(any(PageRequest.List.class))).thenReturn(pageItem);

        // Act & Assert
        mockMvc.perform(get("/jobs/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc")
                        .header(CommonConstants.USER_NAME, "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());

        verify(jobService).getAllJobs(any(PageRequest.List.class));
    }

    @Test
    void changeJobStatus_Success() throws Exception {
        // Arrange
        when(jobService.changeJobStatus("job-uuid", "COMPLETED")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(patch("/jobs/job-uuid/status")
                        .param("status", "COMPLETED")
                        .header(CommonConstants.USER_NAME, "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job status updated successfully"))
                .andExpect(jsonPath("$.data").value(true));

        verify(jobService).changeJobStatus("job-uuid", "COMPLETED");
    }
}