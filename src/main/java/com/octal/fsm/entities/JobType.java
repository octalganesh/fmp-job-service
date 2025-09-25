package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_type")
public class JobType extends AbstractPersistable{

    private String name;

    private String description;

    @ManyToMany(cascade = CascadeType.ALL,targetEntity = JobTask.class)
    private List<JobTask> jobTasks;

}
