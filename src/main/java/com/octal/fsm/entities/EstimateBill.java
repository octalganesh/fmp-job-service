package com.octal.fsm.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "estimates")
@Data
public class EstimateBill extends AbstractPersistable{
    @Column(name = "estimate_id")
    private String estimateId;

    private String jobId;
    private String taskId;
    private String status;

    private BigDecimal subTotal;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private String documentUrl;

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstimateItem> items;

    @OneToMany(mappedBy = "estimate",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstimateDocument> documents;

    @Column(name = "tenant_id")
    private Long tenantId;
}
