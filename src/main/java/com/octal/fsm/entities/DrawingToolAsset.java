package com.octal.fsm.entities;

import com.octal.fsm.entities.enums.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "drawing_tool_assets")
public class DrawingToolAsset extends AbstractPersistable{


    @Enumerated(EnumType.STRING)
    @Column(name = "category_name", nullable = false)
    private Category name;

    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<AssetItem> assets = new ArrayList<>();

}
