package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class JobTaskDTO {

    @Data
    @AllArgsConstructor
    public static class Add {
        private String id;
        private String name;
        private Boolean isActive;
        private String description;
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
    }



}
