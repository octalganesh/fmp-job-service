package com.octal.fsm.entities;

import com.octal.fsm.entities.enums.TaskAssignedType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_task")
public class JobTask extends AbstractPersistable {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "assigned_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskAssignedType assignedType;

    @Column(name = "sequence")
    private Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_type_id")
    private JobType jobType;
}
