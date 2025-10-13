package com.octal.fsm.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document")
@Data
public class Documents extends AbstractPersistable {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "file_type", nullable = false)
    private String fileType;
}
