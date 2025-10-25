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

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "document_type_id")
    private String documentTypeId;

    @Column(name = "attach_type", nullable = false)
    private String attachType;   //Directly Into Job, Job Task and etc

    @Column(name = "attach_type_id", nullable = false)
    private String attachTypeId; // Job Id, Job Task Id and etc

    @Column(name = "uploaded_by_type", nullable = false)
    private String uploadedByType; // Admin, FrontOffice, Technician

    @Column(name = "uploaded_by_type_id", nullable = false)
    private String uploadedByTypeId; // Admin Id, Customer Id, Technician Id

    @Column(name = "uploaded_by_user_name", nullable = false)
    private String uploadedByUserName; // Admin Id, Customer Id, Technician Id
}
