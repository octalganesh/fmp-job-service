package com.octal.fsm.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "create_assembly_component")
@Data
public class CreateAssemblyComponent extends AbstractPersistable{


    private String itemName;
    private BigDecimal quantity;

    @ManyToOne
    @JoinColumn(name = "assembly_queue_id")
    private CreateAssemblyQueue assemblyQueue;
}
