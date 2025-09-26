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
public class JobType extends AbstractPersistable{

    private String name;

    private String description;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    private List<JobTask> jobTasks=new ArrayList<>();

}
