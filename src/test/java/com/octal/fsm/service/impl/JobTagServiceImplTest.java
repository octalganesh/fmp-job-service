package com.octal.fsm.service.impl;

import com.octal.fsm.dto.JobTagDTO;
import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.JobTag;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobTagRepository;
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
class JobTagServiceImplTest {

    @Mock
    private JobTagRepository jobTagRepository;

    @Mock
    private SpecificationFactory<JobTag> jobTagSpecificationFactory;

    @InjectMocks
    private JobTagServiceImpl jobTagService;

    private JobTag testJobTag;
    private JobTagDTO.Add addJobTagDTO;

    @BeforeEach
    void setUp() {
        testJobTag = new JobTag();
        testJobTag.setRecordId(1L);
        testJobTag.setUuid("test-tag-uuid");
        testJobTag.setName("Test Tag");
        testJobTag.setActive(true);
        testJobTag.setDeleted(false);
        testJobTag.setCreatedAt(LocalDateTime.now());
        testJobTag.setUpdatedAt(LocalDateTime.now());

        addJobTagDTO = new JobTagDTO.Add();
        addJobTagDTO.setName("Test Tag");
        addJobTagDTO.setActive(true);
    }

    @Test
    void addJobTag_Success_NewTag() throws CodeException {
        // Arrange
        when(jobTagRepository.existsByName("Test Tag")).thenReturn(false);
        when(jobTagRepository.save(any(JobTag.class))).thenReturn(testJobTag);

        // Act
        String result = jobTagService.addJobTag(addJobTagDTO);

        // Assert
        assertThat(result).isEqualTo("test-tag-uuid");
        verify(jobTagRepository).existsByName("Test Tag");
        verify(jobTagRepository).save(any(JobTag.class));
    }

    @Test
    void addJobTag_Success_UpdateExistingTag() throws CodeException {
        // Arrange
        addJobTagDTO.setId("test-tag-uuid");
        when(jobTagRepository.findByUuid("test-tag-uuid")).thenReturn(Optional.of(testJobTag));
        when(jobTagRepository.existsByNameAndUuidNot("Test Tag", "test-tag-uuid")).thenReturn(false);
        when(jobTagRepository.save(testJobTag)).thenReturn(testJobTag);

        // Act
        String result = jobTagService.addJobTag(addJobTagDTO);

        // Assert
        assertThat(result).isEqualTo("test-tag-uuid");
        verify(jobTagRepository).findByUuid("test-tag-uuid");
        verify(jobTagRepository).existsByNameAndUuidNot("Test Tag", "test-tag-uuid");
        verify(jobTagRepository).save(testJobTag);
    }

    @Test
    void addJobTag_ThrowsException_WhenNameIsEmpty() {
        // Arrange
        addJobTagDTO.setName("");

        // Act & Assert
        assertThatThrownBy(() -> jobTagService.addJobTag(addJobTagDTO))
                .isInstanceOf(CodeException.class)
                .hasMessage("Tag name is required");
    }

    @Test
    void addJobTag_ThrowsException_WhenNameAlreadyExists() {
        // Arrange
        when(jobTagRepository.existsByName("Test Tag")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> jobTagService.addJobTag(addJobTagDTO))
                .isInstanceOf(CodeException.class)
                .hasMessage("Tag name is already exist");
    }

    @Test
    void deleteById_Success() throws CodeException {
        // Arrange
        when(jobTagRepository.findByUuid("test-tag-uuid")).thenReturn(Optional.of(testJobTag));
        when(jobTagRepository.save(testJobTag)).thenReturn(testJobTag);

        // Act
        Boolean result = jobTagService.deleteById("test-tag-uuid");

        // Assert
        assertThat(result).isTrue();
        assertThat(testJobTag.isDeleted()).isTrue();
        verify(jobTagRepository).findByUuid("test-tag-uuid");
        verify(jobTagRepository).save(testJobTag);
    }

    @Test
    void deleteById_ThrowsException_WhenTagNotFound() {
        // Arrange
        when(jobTagRepository.findByUuid("test-tag-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobTagService.deleteById("test-tag-uuid"))
                .isInstanceOf(CodeException.class)
                .hasMessageContaining("JobTag not found by id : test-tag-uuid");
    }

    @Test
    void getJobTagByUuid_Success() throws CodeException {
        // Arrange
        when(jobTagRepository.findByUuid("test-tag-uuid")).thenReturn(Optional.of(testJobTag));

        // Act
        JobTypeDTO.Detail result = jobTagService.getJobTagByUuid("test-tag-uuid");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Tag");
        assertThat(result.getId()).isEqualTo("test-tag-uuid");
        verify(jobTagRepository).findByUuid("test-tag-uuid");
    }

    @Test
    void getJobTagByUuid_ThrowsException_WhenTagNotFound() {
        // Arrange
        when(jobTagRepository.findByUuid("test-tag-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobTagService.getJobTagByUuid("test-tag-uuid"))
                .isInstanceOf(CodeException.class)
                .hasMessageContaining("JobTag not found by id : test-tag-uuid");
    }
}