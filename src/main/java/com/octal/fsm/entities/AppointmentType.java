package com.octal.fsm.entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table(name = "appointment_type")
public class AppointmentType extends AbstractPersistable {


    private String name;

    private String description;

    @Lob
    @Column(name = "job_type_ids")
    @ElementCollection
    private List<String> jobTypeIds;


}
