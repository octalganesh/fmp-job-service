package com.octal.fsm.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "inventory_items")
@Data
public class InventoryItems extends AbstractPersistable{

    public String listId;
    public String fullName;
    public String quantity;

    @ManyToOne
    @JoinColumn(name = "inventory_part_id", nullable = false)
    private InventoryPart inventoryPart;
}
