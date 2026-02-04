package com.octal.fsm.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "create_assembly_queue")
@Data
public class CreateAssemblyQueue extends AbstractPersistable{

    @Column(name = "assembly_name", unique = true, nullable = false)
    private String assemblyName;
    private String incomeAccount;
    private String cogsAccount;
    private String assetAccount;
    private BigDecimal salesPrice;
    private String listId;
    private String editSequence;
    private String syncStatus;   // QUEUED,CREATED
    private String activeToken;

    @OneToMany(
            mappedBy = "assemblyQueue",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CreateAssemblyComponent> components = new ArrayList<>();
}

