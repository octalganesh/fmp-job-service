package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class JobNotesDTO {

    @Data
    @AllArgsConstructor
    public static class Add {
        private String jobId;
        private String notes;
        private String createdBy;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Details{
        private String id;
        private Boolean active;
        private String jobId;
        private String notes;
        private String createdBy;
        private String createdAt;
        private String updatedAt;
    }



}
