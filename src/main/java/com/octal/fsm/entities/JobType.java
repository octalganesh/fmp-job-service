package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_type")
public class JobType extends AbstractPersistable {

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 512)
    private String description;

    @OneToMany(
            mappedBy = "jobType",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sequence ASC")
    private List<JobTask> jobTasks = new ArrayList<>();

    @Column(name = "tenant_id")
    private Long tenantId;

}


