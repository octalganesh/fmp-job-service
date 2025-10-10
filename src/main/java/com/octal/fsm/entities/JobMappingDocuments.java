package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_mapping_documents")
@Data
public class JobMappingDocuments extends AbstractPersistable {

    // 🔁 Many-to-One mapping to Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "document_id", nullable = false)
    private String documentId;

}

