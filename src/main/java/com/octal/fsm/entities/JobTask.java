package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_task")
public class JobTask extends AbstractPersistable{

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 512)
    private String description;
}

