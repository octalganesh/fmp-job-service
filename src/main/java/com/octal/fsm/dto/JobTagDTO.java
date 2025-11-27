package com.octal.fsm.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class JobTagDTO {

    @Data
    @AllArgsConstructor
    public static class Add {
        private String id;
        private String name;
        private String tagColor;
        private boolean isActive;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Detail {
        private String id;
        private String name;
        private String tagColor;
        private String createdAt;
        private String updatedAt;
        private Boolean isActive;

    }
}


