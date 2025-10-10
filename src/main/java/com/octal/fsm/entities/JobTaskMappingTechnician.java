package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_task_mapping_technician")
@Data
public class JobTaskMappingTechnician extends AbstractPersistable {

    @Column(name = "job_task_mapping_id", nullable = false)
    private String jobTaskMappingId;

    @Column(name = "technician_id", nullable = false)
    private String technicianId;

    @Column(name = "task_status", nullable = false)
    private String taskStatus;

    @Column(name = "note")
    private String note;

}

