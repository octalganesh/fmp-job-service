package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_call_history")
@Data
public class JobCallHistory extends AbstractPersistable {

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Column(name = "call_note", length = 1000, nullable = false)
    private String callNote;

    @Column(name = "created_by_name", nullable = false)
    private String createdByName;

    @Column(name = "created_by_id", nullable = false)
    private String createdById;

}

