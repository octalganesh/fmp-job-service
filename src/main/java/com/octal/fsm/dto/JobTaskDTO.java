package com.octal.fsm.dto;

import com.octal.fsm.entities.enums.TaskAssignedType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class JobTaskDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {
        private String id;
        private String name;
        private String jobTypeId;
        private Boolean isActive;
        private String description;
        private TaskAssignedType assignedType;
        private Integer sequence;
        private String statusMasterId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {
        private String id;
        private String name;
        private String createdAt;
        private String updatedAt;
        private Boolean isActive;
        private String description;
        private TaskAssignedType assignedType;
        private String statusMasterId;
        private Integer sequence;
    }


}
