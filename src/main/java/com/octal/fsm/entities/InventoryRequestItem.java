package com.octal.fsm.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "inventory_request_item")
@Data
public class InventoryRequestItem extends AbstractPersistable{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private InventoryRequest inventoryRequest;

    @Column(name = "inventory_list_id",nullable = false)
    private String inventoryListId;

    @Column(name = "inventory_name")
    private String inventoryName;

    @Column(name = "requested_qty",nullable = false)
    private Integer requestedQty;

    @Column(name = "approved_qty")
    private Integer approvedQty;
}
