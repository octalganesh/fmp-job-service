package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class JobCallDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {
        private String jobId;
        private String callNote;
        private String createdByName;
        private String createdById;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListResponse {
        private String id;
        private String jobId;
        private String callNote;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        private String createdByName;
        private String createdById;
    }
}
