package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_tag")
public class JobTag extends AbstractPersistable{

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "tag_color", nullable = false)
    private String tagColor;

}
