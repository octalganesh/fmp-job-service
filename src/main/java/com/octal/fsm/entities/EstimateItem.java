package com.octal.fsm.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "estimate_items")
@Data
public class EstimateItem extends AbstractPersistable{

    private String listId;
    private String itemName;
    private String itemType;
    private BigDecimal unitCost;
    private Integer quantity;
    private BigDecimal totalValue;

    @ManyToOne
    @JoinColumn(name = "estimate_id")
    private EstimateBill estimate;
}
