package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data

public class EmailTemplateDto {


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmailTemplateRequest {
        private String templateName;
        private Map<String, Object> props;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class EmailTemplateResponse {
        private String subject;
        private String body;
    }
}