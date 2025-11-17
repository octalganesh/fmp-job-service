package com.octal.fsm.entities;

import com.octal.fsm.entities.enums.TaskAssignedType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_mapping_tasks")
@Data
public class JobMappingTask extends AbstractPersistable {

    // 🔁 Many-to-One mapping to Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "task_show_id", nullable = false)
    private String taskShowId;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Lob
    @Column(name = "document_type_id")
    private String documentTypeId;  // An List of DocumentType IDs in JSON format

    @Column(name = "task_sequence")
    private Integer taskSequence;

    @Column(name = "job_task_status")
    private String jobTaskStatus; // todo job status when this task is the current task.

    @Column(name = "assign_type")
    private TaskAssignedType assignType;


}

