package com.octal.fsm.service.impl;

import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobTypeRepository;
import com.octal.fsm.specification.SpecificationFactory;
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

@ExtendWith(MockitoExtension.class)
class JobTypeServiceImplTest {

    @Mock
    private JobTypeRepository jobTypeRepository;

    @Mock
    private SpecificationFactory<JobType> jobTypeSpecificationFactory;

    @InjectMocks
    private JobTypeServiceImpl jobTypeService;

    private JobType testJobType;
    private JobTypeDTO.Add addJobTypeDTO;

    @BeforeEach
    void setUp() {
        testJobType = new JobType();
        testJobType.setRecordId(1L);
        testJobType.setUuid("test-type-uuid");
        testJobType.setName("Test Type");
        testJobType.setDescription("Test Description");
        testJobType.setActive(true);
        testJobType.setDeleted(false);
        testJobType.setCreatedAt(LocalDateTime.now());
        testJobType.setUpdatedAt(LocalDateTime.now());

        addJobTypeDTO = new JobTypeDTO.Add();
        addJobTypeDTO.setName("Test Type");
        addJobTypeDTO.setDescription("Test Description");
        addJobTypeDTO.setIsActive(true);
    }

    @Test
    void addJobType_Success_NewType() throws CodeException {
        // Arrange
        when(jobTypeRepository.save(any(JobType.class))).thenReturn(testJobType);

        // Act
        String result = jobTypeService.addJobType(addJobTypeDTO);

        // Assert
        assertThat(result).isEqualTo("test-type-uuid");
        verify(jobTypeRepository).save(any(JobType.class));
    }

    @Test
    void addJobType_Success_UpdateExistingType() throws CodeException {
        // Arrange
        addJobTypeDTO.setId("test-type-uuid");
        when(jobTypeRepository.findByUuid("test-type-uuid")).thenReturn(Optional.of(testJobType));
        when(jobTypeRepository.save(testJobType)).thenReturn(testJobType);

        // Act
        String result = jobTypeService.addJobType(addJobTypeDTO);

        // Assert
        assertThat(result).isEqualTo("test-type-uuid");
        verify(jobTypeRepository, times(2)).findByUuid("test-type-uuid");
        verify(jobTypeRepository).save(testJobType);
    }

    @Test
    void addJobType_ThrowsException_WhenTypeNotFoundForUpdate() {
        // Arrange
        addJobTypeDTO.setId("test-type-uuid");
        when(jobTypeRepository.findByUuid("test-type-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobTypeService.addJobType(addJobTypeDTO))
                .isInstanceOf(CodeException.class)
                .hasMessage("jobType not Found!");
    }

    @Test
    void deleteById_Success() throws CodeException {
        // Arrange
        when(jobTypeRepository.findByUuid("test-type-uuid")).thenReturn(Optional.of(testJobType));
        when(jobTypeRepository.save(testJobType)).thenReturn(testJobType);

        // Act
        Boolean result = jobTypeService.deleteById("test-type-uuid");

        // Assert
        assertThat(result).isTrue();
        assertThat(testJobType.isDeleted()).isTrue();
        verify(jobTypeRepository).findByUuid("test-type-uuid");
        verify(jobTypeRepository).save(testJobType);
    }

    @Test
    void deleteById_ThrowsException_WhenTypeNotFound() {
        // Arrange
        when(jobTypeRepository.findByUuid("test-type-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobTypeService.deleteById("test-type-uuid"))
                .isInstanceOf(CodeException.class)
                .hasMessageContaining("JobType not found by id : test-type-uuid");
    }

    @Test
    void getJobTypeByUuid_Success() throws CodeException {
        // Arrange
        when(jobTypeRepository.findByUuid("test-type-uuid")).thenReturn(Optional.of(testJobType));

        // Act
        JobTypeDTO.Detail result = jobTypeService.getJobTypeByUuid("test-type-uuid");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Type");
        assertThat(result.getId()).isEqualTo("test-type-uuid");
        verify(jobTypeRepository).findByUuid("test-type-uuid");
    }

    @Test
    void getJobTypeByUuid_ThrowsException_WhenTypeNotFound() {
        // Arrange
        when(jobTypeRepository.findByUuid("test-type-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobTypeService.getJobTypeByUuid("test-type-uuid"))
                .isInstanceOf(CodeException.class)
                .hasMessageContaining("JobType not found by id : test-type-uuid");
    }
}