package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_status_master")
@Data
public class JobStatusMaster extends AbstractPersistable{
    private String name;
    private String description;
    private Integer sequenceOrder;
    private String colorCode; // todo need to discuss
    private Long tenantId;
}
