package com.octal.fsm.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_request")
@Data
public class InventoryRequest extends AbstractPersistable{

    @Column(name = "technician_id", nullable = false)
    private String technicianId;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "request_show_id", nullable = false)
    private String requestShowId;

    @Column(name = "comment")
    private String comment;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @OneToMany(mappedBy = "inventoryRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryRequestItem> items = new ArrayList<>();
}
