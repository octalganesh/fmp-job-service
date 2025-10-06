package com.octal.fsm.repositories;

import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class JobRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobRepository jobRepository;

    private Job testJob;
    private JobType testJobType;

    @BeforeEach
    void setUp() {
        // Create and persist a JobType
        testJobType = new JobType();
        testJobType.setName("Test Job Type");
        testJobType.setDescription("Test job type description");
        testJobType.setUuid(UUID.randomUUID().toString());
        testJobType = entityManager.persistAndFlush(testJobType);

        // Create a test Job
        testJob = new Job();
        testJob.setJobType(testJobType);
        testJob.setJobStatus("SCHEDULED");
        testJob.setJobSummary("Test job summary");
        testJob.setPriority("HIGH");
        testJob.setEstimatedCost(1000.0);
        testJob.setJobTimeDuration("2 hours");
        testJob.setAssignedDateTime(LocalDateTime.now());
        testJob.setActive(true);
        testJob.setInvoiceStatus("PENDING");
        testJob.setPaymentStatus("PENDING");
        testJob.setUuid(UUID.randomUUID().toString());
        testJob.setDeleted(false);
        testJob = entityManager.persistAndFlush(testJob);
    }

    @Test
    void testFindByUuidAndDeletedFalse_WhenJobExists_ShouldReturnJob() {
        // When
        Optional<Job> result = jobRepository.findByUuidAndDeletedFalse(testJob.getUuid());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(testJob.getUuid());
        assertThat(result.get().getJobSummary()).isEqualTo("Test job summary");
        assertThat(result.get().isDeleted()).isFalse();
    }

    @Test
    void testFindByUuidAndDeletedFalse_WhenJobDeleted_ShouldReturnEmpty() {
        // Given
        testJob.setDeleted(true);
        entityManager.persistAndFlush(testJob);

        // When
        Optional<Job> result = jobRepository.findByUuidAndDeletedFalse(testJob.getUuid());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByUuidAndDeletedFalse_WhenJobNotExists_ShouldReturnEmpty() {
        // When
        Optional<Job> result = jobRepository.findByUuidAndDeletedFalse("non-existent-uuid");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testFindAllByDeletedFalse_ShouldReturnOnlyNonDeletedJobs() {
        // Given
        Job deletedJob = new Job();
        deletedJob.setJobType(testJobType);
        deletedJob.setJobStatus("CANCELLED");
        deletedJob.setJobSummary("Deleted job");
        deletedJob.setPriority("LOW");
        deletedJob.setUuid(UUID.randomUUID().toString());
        deletedJob.setDeleted(true);
        entityManager.persistAndFlush(deletedJob);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Job> result = jobRepository.findAllByDeletedFalse(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUuid()).isEqualTo(testJob.getUuid());
        assertThat(result.getContent().get(0).isDeleted()).isFalse();
    }

    @Test
    void testSaveJob_ShouldPersistJobSuccessfully() {
        // Given
        Job newJob = new Job();
        newJob.setJobType(testJobType);
        newJob.setJobStatus("NEW");
        newJob.setJobSummary("New job summary");
        newJob.setPriority("MEDIUM");
        newJob.setEstimatedCost(500.0);
        newJob.setUuid(UUID.randomUUID().toString());
        newJob.setDeleted(false);

        // When
        Job savedJob = jobRepository.save(newJob);

        // Then
        assertThat(savedJob.getRecordId()).isNotNull();
        assertThat(savedJob.getJobSummary()).isEqualTo("New job summary");
        assertThat(savedJob.getJobStatus()).isEqualTo("NEW");
        assertThat(savedJob.getPriority()).isEqualTo("MEDIUM");
        assertThat(savedJob.getEstimatedCost()).isEqualTo(500.0);

        // Verify it can be retrieved
        Optional<Job> retrievedJob = jobRepository.findByUuidAndDeletedFalse(savedJob.getUuid());
        assertThat(retrievedJob).isPresent();
        assertThat(retrievedJob.get().getJobSummary()).isEqualTo("New job summary");
    }

    @Test
    void testDeleteJob_ShouldMarkAsDeleted() {
        // Given
        String jobUuid = testJob.getUuid();

        // When
        testJob.setDeleted(true);
        jobRepository.save(testJob);

        // Then
        Optional<Job> result = jobRepository.findByUuidAndDeletedFalse(jobUuid);
        assertThat(result).isEmpty();

        // But should exist when searching without deleted filter
        Optional<Job> deletedJob = jobRepository.findById(testJob.getRecordId());
        assertThat(deletedJob).isPresent();
        assertThat(deletedJob.get().isDeleted()).isTrue();
    }

    @Test
    void testUpdateJob_ShouldUpdateJobSuccessfully() {
        // Given
        String originalSummary = testJob.getJobSummary();
        String newSummary = "Updated job summary";

        // When
        testJob.setJobSummary(newSummary);
        testJob.setJobStatus("IN_PROGRESS");
        Job updatedJob = jobRepository.save(testJob);

        // Then
        assertThat(updatedJob.getJobSummary()).isEqualTo(newSummary);
        assertThat(updatedJob.getJobStatus()).isEqualTo("IN_PROGRESS");
        assertThat(updatedJob.getJobSummary()).isNotEqualTo(originalSummary);

        // Verify changes are persisted
        Optional<Job> retrievedJob = jobRepository.findByUuidAndDeletedFalse(testJob.getUuid());
        assertThat(retrievedJob).isPresent();
        assertThat(retrievedJob.get().getJobSummary()).isEqualTo(newSummary);
        assertThat(retrievedJob.get().getJobStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void testFindAll_WithPagination_ShouldReturnPagedResults() {
        // Given - Create additional jobs
        for (int i = 0; i < 5; i++) {
            Job job = new Job();
            job.setJobType(testJobType);
            job.setJobStatus("SCHEDULED");
            job.setJobSummary("Job summary " + i);
            job.setPriority("LOW");
            job.setUuid(UUID.randomUUID().toString());
            job.setDeleted(false);
            entityManager.persistAndFlush(job);
        }

        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<Job> result = jobRepository.findAllByDeletedFalse(pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6); // 1 from setUp + 5 created
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void testSaveAndFindByUuidAndDeletedFalse() {
        Job job = new Job();
        job.setJobStatus("NEW");
        job.setDeleted(false);
        Job saved = jobRepository.save(job);
        Optional<Job> found = jobRepository.findByUuidAndDeletedFalse(saved.getUuid());
        assertThat(found).isPresent();
        assertThat(found.get().getJobStatus()).isEqualTo("NEW");
    }

    @Test
    void testFindAllByDeletedFalse() {
        Job job1 = new Job();
        job1.setJobStatus("ACTIVE");
        job1.setDeleted(false);
        jobRepository.save(job1);
        Job job2 = new Job();
        job2.setJobStatus("INACTIVE");
        job2.setDeleted(true);
        jobRepository.save(job2);
        assertThat(jobRepository.findAllByDeletedFalse(org.springframework.data.domain.PageRequest.of(0, 10)).getContent())
            .extracting(Job::getJobStatus)
            .contains("ACTIVE");
    }
}