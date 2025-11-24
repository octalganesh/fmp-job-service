package com.octal.fsm.entities;

import com.google.cloud.firestore.Blob;
import com.octal.fsm.entities.enums.SystemEventType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "system_event_log")
@Data
@NoArgsConstructor
public class SystemEventLog extends AbstractPersistable{

    @Enumerated(EnumType.STRING)
    private SystemEventType eventType;

    @Lob
    @Column(name = "description")
    private String description;

    private String referenceId;//job id

    private String performedBy;
    private String profileUrl;
}
