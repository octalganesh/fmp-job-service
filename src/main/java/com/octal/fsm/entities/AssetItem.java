package com.octal.fsm.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "assest_items")
public class AssetItem extends AbstractPersistable{

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "width")
    private Float width = 100f;

    @Column(name = "height")
    private Float height = 100f;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private DrawingToolAsset category;
}
