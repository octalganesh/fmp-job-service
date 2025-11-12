package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_history")
@Data
public class JobHistory extends AbstractPersistable {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "front_office_id")
    private String frontOfficeId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "job_id")
    private String jobId;

}

