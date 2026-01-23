package com.octal.fsm.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class GeneralSettingDTO {

    @Data
    public static class Add {
        private String key;
        private String value;
        private String description;
    }

    @Data
    @RequiredArgsConstructor
    public static class Details {
        private String id;
        private String key;
        private String value;
        private String description;
        private String createdBy;
        private Long tenantId;
        private Boolean isActive;


    }

}
