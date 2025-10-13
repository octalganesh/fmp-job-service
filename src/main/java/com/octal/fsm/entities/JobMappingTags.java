package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_mapping_tags")
@Data
public class JobMappingTags extends AbstractPersistable {

    // 🔁 Many-to-One mapping to Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "tag_id", nullable = false)
    private String tagId;

}

