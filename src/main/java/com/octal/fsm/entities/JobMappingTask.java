package com.octal.fsm.entities;

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

    @Column(name= "task_show_id", nullable = false)
    private String taskShowId;

    @Lob
    @Column(name = "document_type_id")
    private String documentTypeId;  // An List of DocumentType IDs in JSON format


}

