package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_notes")
@Data
public class JobNotes extends AbstractPersistable{

        @Column(name = "job_id", nullable = false)
        private String jobId;

        @Lob
        @Column(name = "notes", nullable = false)
        private String notes;

        @Column(name = "created_by")
        private String createdBy;

}
