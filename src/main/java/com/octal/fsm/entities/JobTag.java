package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "job_tag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "tenant_id"})
)
public class JobTag extends AbstractPersistable {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "tag_color", nullable = false)
    private String tagColor;

}
