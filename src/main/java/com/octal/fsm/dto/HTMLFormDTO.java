package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class HTMLFormDTO {

    @Data
    @AllArgsConstructor
    public static class Add {
        private String taskId;
        private String name;
        private String content;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Details {
        private String id;
        private Boolean active;
        private String name;
        private String content;
        private String createdAt;
        private String updatedAt;
    }

}
