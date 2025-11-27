package com.octal.fsm.dto;

import com.octal.fsm.entities.HTMLFormPage;
import com.octal.fsm.entities.enums.TaskAssignedType;
import lombok.Data;

import java.util.List;

@Data
public class JobMappingTaskDTO {

    private String uuid;
    private String taskId;
    private String taskShowId;
    private String taskName;
    private String documentTypeId;
    private Integer taskSequence;
    private String jobTaskStatus;
    private TaskAssignedType assignType;
    private String note;
    private List<HTMLFormPage> htmlFormPages;
    private List<DocumentDTO.Add> documents;
}
