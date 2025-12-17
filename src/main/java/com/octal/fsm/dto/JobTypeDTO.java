package com.octal.fsm.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class JobTypeDTO {

    @Data
    @AllArgsConstructor
    public static class Add {
        private String id;
        private String name;
        private Boolean isActive;
        private String description;
        private List<JobTaskDTO.Add> jobTasks;
        private List<String> documents;
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
        private List<JobTaskDTO.Detail> jobTasks;
        private List<String> documents;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailWithoutJobTasks {
        private String id;
        private String name;
        private String createdAt;
        private String updatedAt;
        private Boolean isActive;
        private String description;
    }


}


