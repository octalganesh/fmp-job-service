package com.octal.fsm.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "drawing_tool_assets")
public class DrawingToolAsset extends AbstractPersistable{

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Lob
    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_type", nullable = false)
    private String fileType;
}
