package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class JobTagRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobTagRepository jobTagRepository;

    private JobTag testJobTag;

    @BeforeEach
    void setUp() {
        testJobTag = new JobTag();
        testJobTag.setName("TestTag");
        testJobTag.setUuid(UUID.randomUUID().toString());
        testJobTag = entityManager.persistAndFlush(testJobTag);
    }

    @Test
    void testFindByUuid_WhenJobTagExists_ShouldReturnJobTag() {
        // When
        Optional<JobTag> result = jobTagRepository.findByUuid(testJobTag.getUuid());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(testJobTag.getUuid());
        assertThat(result.get().getName()).isEqualTo("TestTag");
    }

    @Test
    void testFindByUuid_WhenJobTagNotExists_ShouldReturnEmpty() {
        // When
        Optional<JobTag> result = jobTagRepository.findByUuid("non-existent-uuid");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testExistsByName_WhenNameExists_ShouldReturnTrue() {
        // When
        Boolean result = jobTagRepository.existsByName("TestTag");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testExistsByName_WhenNameNotExists_ShouldReturnFalse() {
        // When
        Boolean result = jobTagRepository.existsByName("NonExistentTag");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testExistsByNameAndUuidNot_WhenNameExistsButDifferentUuid_ShouldReturnTrue() {
        // When
        Boolean result = jobTagRepository.existsByNameAndUuidNot("TestTag", "different-uuid");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testExistsByNameAndUuidNot_WhenNameExistsButSameUuid_ShouldReturnFalse() {
        // When
        Boolean result = jobTagRepository.existsByNameAndUuidNot("TestTag", testJobTag.getUuid());

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testExistsByNameAndUuidNot_WhenNameNotExists_ShouldReturnFalse() {
        // When
        Boolean result = jobTagRepository.existsByNameAndUuidNot("NonExistentTag", "any-uuid");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testSaveJobTag_ShouldPersistJobTagSuccessfully() {
        // Given
        JobTag newJobTag = new JobTag();
        newJobTag.setName("NewTag");
        newJobTag.setUuid(UUID.randomUUID().toString());

        // When
        JobTag savedJobTag = jobTagRepository.save(newJobTag);

        // Then
        assertThat(savedJobTag.getRecordId()).isNotNull();
        assertThat(savedJobTag.getName()).isEqualTo("NewTag");
        assertThat(savedJobTag.getUuid()).isNotNull();

        // Verify it can be retrieved
        Optional<JobTag> retrievedJobTag = jobTagRepository.findByUuid(savedJobTag.getUuid());
        assertThat(retrievedJobTag).isPresent();
        assertThat(retrievedJobTag.get().getName()).isEqualTo("NewTag");
    }

    @Test
    void testUpdateJobTag_ShouldUpdateJobTagSuccessfully() {
        // Given
        String originalName = testJobTag.getName();
        String newName = "UpdatedTag";

        // When
        testJobTag.setName(newName);
        JobTag updatedJobTag = jobTagRepository.save(testJobTag);

        // Then
        assertThat(updatedJobTag.getName()).isEqualTo(newName);
        assertThat(updatedJobTag.getName()).isNotEqualTo(originalName);

        // Verify changes are persisted
        Optional<JobTag> retrievedJobTag = jobTagRepository.findByUuid(testJobTag.getUuid());
        assertThat(retrievedJobTag).isPresent();
        assertThat(retrievedJobTag.get().getName()).isEqualTo(newName);
    }

    @Test
    void testDeleteJobTag_ShouldRemoveJobTag() {
        // Given
        Long jobTagId = testJobTag.getRecordId();
        String jobTagUuid = testJobTag.getUuid();

        // When
        jobTagRepository.delete(testJobTag);
        entityManager.flush();

        // Then
        Optional<JobTag> result = jobTagRepository.findByUuid(jobTagUuid);
        assertThat(result).isEmpty();

        Optional<JobTag> resultById = jobTagRepository.findById(jobTagId);
        assertThat(resultById).isEmpty();
    }

    @Test
    void testFindAll_ShouldReturnAllJobTags() {
        // Given - Create additional job tags
        JobTag tag1 = new JobTag();
        tag1.setName("Tag1");
        tag1.setUuid(UUID.randomUUID().toString());
        entityManager.persistAndFlush(tag1);

        JobTag tag2 = new JobTag();
        tag2.setName("Tag2");
        tag2.setUuid(UUID.randomUUID().toString());
        entityManager.persistAndFlush(tag2);

        // When
        var allTags = jobTagRepository.findAll();

        // Then
        assertThat(allTags).hasSize(3); // testJobTag + tag1 + tag2
        assertThat(allTags.stream().map(JobTag::getName))
                .containsExactlyInAnyOrder("TestTag", "Tag1", "Tag2");
    }

    @Test
    void testJobTagNameUniqueness() {
        // Given
        JobTag duplicateNameTag = new JobTag();
        duplicateNameTag.setName("TestTag"); // Same name as testJobTag
        duplicateNameTag.setUuid(UUID.randomUUID().toString());

        // When & Then
        // This should throw a constraint violation exception due to unique constraint
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> {
                    jobTagRepository.save(duplicateNameTag);
                    entityManager.flush();
                }
        );
    }
}