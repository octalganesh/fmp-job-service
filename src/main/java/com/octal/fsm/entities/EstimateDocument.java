package com.octal.fsm.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "estimate_document")
@Data
public class EstimateDocument extends AbstractPersistable{

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_type")
    private String documentType;
    // ESTIMATE_PDF, REVISION_PDF, ATTACHMENT, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_id", nullable = false)
    private EstimateBill estimate;
}
