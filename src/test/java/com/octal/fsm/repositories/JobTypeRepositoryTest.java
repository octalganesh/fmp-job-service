package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobTask;
import com.octal.fsm.entities.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class JobTypeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobTypeRepository jobTypeRepository;

    private JobType testJobType;

    @BeforeEach
    void setUp() {
        testJobType = new JobType();
        testJobType.setName("Test Job Type");
        testJobType.setDescription("Test job type description");
        testJobType.setUuid(UUID.randomUUID().toString());
        testJobType.setJobTasks(new ArrayList<>());
        testJobType = entityManager.persistAndFlush(testJobType);
    }

    @Test
    void testFindByUuid_WhenJobTypeExists_ShouldReturnJobType() {
        // When
        Optional<JobType> result = jobTypeRepository.findByUuid(testJobType.getUuid());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(testJobType.getUuid());
        assertThat(result.get().getName()).isEqualTo("Test Job Type");
        assertThat(result.get().getDescription()).isEqualTo("Test job type description");
    }

    @Test
    void testFindByUuid_WhenJobTypeNotExists_ShouldReturnEmpty() {
        // When
        Optional<JobType> result = jobTypeRepository.findByUuid("non-existent-uuid");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testSaveJobType_ShouldPersistJobTypeSuccessfully() {
        // Given
        JobType newJobType = new JobType();
        newJobType.setName("New Job Type");
        newJobType.setDescription("New job type description");
        newJobType.setUuid(UUID.randomUUID().toString());
        newJobType.setJobTasks(new ArrayList<>());

        // When
        JobType savedJobType = jobTypeRepository.save(newJobType);

        // Then
        assertThat(savedJobType.getRecordId()).isNotNull();
        assertThat(savedJobType.getName()).isEqualTo("New Job Type");
        assertThat(savedJobType.getDescription()).isEqualTo("New job type description");
        assertThat(savedJobType.getUuid()).isNotNull();

        // Verify it can be retrieved
        Optional<JobType> retrievedJobType = jobTypeRepository.findByUuid(savedJobType.getUuid());
        assertThat(retrievedJobType).isPresent();
        assertThat(retrievedJobType.get().getName()).isEqualTo("New Job Type");
    }

    @Test
    void testSaveJobTypeWithTasks_ShouldPersistJobTypeAndTasksSuccessfully() {
        // Given
        JobType jobTypeWithTasks = new JobType();
        jobTypeWithTasks.setName("Job Type With Tasks");
        jobTypeWithTasks.setDescription("Job type with associated tasks");
        jobTypeWithTasks.setUuid(UUID.randomUUID().toString());

        // Create job tasks
        List<JobTask> tasks = new ArrayList<>();
        
        JobTask task1 = new JobTask();
        task1.setName("Task 1");
        task1.setDescription("Description for task 1");
        task1.setUuid(UUID.randomUUID().toString());
        tasks.add(task1);

        JobTask task2 = new JobTask();
        task2.setName("Task 2");
        task2.setDescription("Description for task 2");
        task2.setUuid(UUID.randomUUID().toString());
        tasks.add(task2);

        jobTypeWithTasks.setJobTasks(tasks);

        // When
        JobType savedJobType = jobTypeRepository.save(jobTypeWithTasks);

        // Then
        assertThat(savedJobType.getRecordId()).isNotNull();
        assertThat(savedJobType.getJobTasks()).hasSize(2);
        assertThat(savedJobType.getJobTasks().get(0).getName()).isEqualTo("Task 1");
        assertThat(savedJobType.getJobTasks().get(1).getName()).isEqualTo("Task 2");

        // Verify persistence
        Optional<JobType> retrievedJobType = jobTypeRepository.findByUuid(savedJobType.getUuid());
        assertThat(retrievedJobType).isPresent();
        assertThat(retrievedJobType.get().getJobTasks()).hasSize(2);
    }

    @Test
    void testUpdateJobType_ShouldUpdateJobTypeSuccessfully() {
        // Given
        String originalName = testJobType.getName();
        String originalDescription = testJobType.getDescription();
        String newName = "Updated Job Type";
        String newDescription = "Updated job type description";

        // When
        testJobType.setName(newName);
        testJobType.setDescription(newDescription);
        JobType updatedJobType = jobTypeRepository.save(testJobType);

        // Then
        assertThat(updatedJobType.getName()).isEqualTo(newName);
        assertThat(updatedJobType.getDescription()).isEqualTo(newDescription);
        assertThat(updatedJobType.getName()).isNotEqualTo(originalName);
        assertThat(updatedJobType.getDescription()).isNotEqualTo(originalDescription);

        // Verify changes are persisted
        Optional<JobType> retrievedJobType = jobTypeRepository.findByUuid(testJobType.getUuid());
        assertThat(retrievedJobType).isPresent();
        assertThat(retrievedJobType.get().getName()).isEqualTo(newName);
        assertThat(retrievedJobType.get().getDescription()).isEqualTo(newDescription);
    }

    @Test
    void testDeleteJobType_ShouldRemoveJobType() {
        // Given
        Long jobTypeId = testJobType.getRecordId();
        String jobTypeUuid = testJobType.getUuid();

        // When
        jobTypeRepository.delete(testJobType);
        entityManager.flush();

        // Then
        Optional<JobType> result = jobTypeRepository.findByUuid(jobTypeUuid);
        assertThat(result).isEmpty();

        Optional<JobType> resultById = jobTypeRepository.findById(jobTypeId);
        assertThat(resultById).isEmpty();
    }

    @Test
    void testDeleteJobTypeWithTasks_ShouldRemoveJobTypeAndOrphanTasks() {
        // Given
        JobType jobTypeWithTasks = new JobType();
        jobTypeWithTasks.setName("Job Type With Tasks");
        jobTypeWithTasks.setDescription("Job type with tasks to be deleted");
        jobTypeWithTasks.setUuid(UUID.randomUUID().toString());

        List<JobTask> tasks = new ArrayList<>();
        JobTask task = new JobTask();
        task.setName("Task to be deleted");
        task.setDescription("Task description");
        task.setUuid(UUID.randomUUID().toString());
        tasks.add(task);

        jobTypeWithTasks.setJobTasks(tasks);
        JobType savedJobType = entityManager.persistAndFlush(jobTypeWithTasks);

        // When
        jobTypeRepository.delete(savedJobType);
        entityManager.flush();

        // Then
        Optional<JobType> result = jobTypeRepository.findByUuid(savedJobType.getUuid());
        assertThat(result).isEmpty();
        
        // Tasks should also be removed due to orphanRemoval = true
        var remainingTasks = entityManager.getEntityManager()
                .createQuery("SELECT t FROM JobTask t", JobTask.class)
                .getResultList();
        assertThat(remainingTasks.stream()
                .noneMatch(t -> t.getName().equals("Task to be deleted")))
                .isTrue();
    }

    @Test
    void testFindAll_ShouldReturnAllJobTypes() {
        // Given - Create additional job types
        JobType jobType1 = new JobType();
        jobType1.setName("Job Type 1");
        jobType1.setDescription("Description 1");
        jobType1.setUuid(UUID.randomUUID().toString());
        jobType1.setJobTasks(new ArrayList<>());
        entityManager.persistAndFlush(jobType1);

        JobType jobType2 = new JobType();
        jobType2.setName("Job Type 2");
        jobType2.setDescription("Description 2");
        jobType2.setUuid(UUID.randomUUID().toString());
        jobType2.setJobTasks(new ArrayList<>());
        entityManager.persistAndFlush(jobType2);

        // When
        var allJobTypes = jobTypeRepository.findAll();

        // Then
        assertThat(allJobTypes).hasSize(3); // testJobType + jobType1 + jobType2
        assertThat(allJobTypes.stream().map(JobType::getName))
                .containsExactlyInAnyOrder("Test Job Type", "Job Type 1", "Job Type 2");
    }

    @Test
    void testUpdateJobTypeTasks_ShouldUpdateTasksSuccessfully() {
        // Given
        JobTask newTask = new JobTask();
        newTask.setName("New Task");
        newTask.setDescription("New task description");
        newTask.setUuid(UUID.randomUUID().toString());

        // When
        testJobType.getJobTasks().add(newTask);
        JobType updatedJobType = jobTypeRepository.save(testJobType);

        // Then
        assertThat(updatedJobType.getJobTasks()).hasSize(1);
        assertThat(updatedJobType.getJobTasks().get(0).getName()).isEqualTo("New Task");

        // Verify persistence
        Optional<JobType> retrievedJobType = jobTypeRepository.findByUuid(testJobType.getUuid());
        assertThat(retrievedJobType).isPresent();
        assertThat(retrievedJobType.get().getJobTasks()).hasSize(1);
        assertThat(retrievedJobType.get().getJobTasks().get(0).getName()).isEqualTo("New Task");
    }
}