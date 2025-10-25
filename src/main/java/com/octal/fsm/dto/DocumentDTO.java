package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class DocumentDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {
        @NotBlank
        private String fileName;
        @NotBlank
        private String documentUrl;
        private String thumbnail;
        private String documentTypeId;
        @NotBlank
        private String fileType;
        @NotBlank
        private String attachType;
        @NotBlank
        private String attachTypeId;
        @NotBlank
        private String uploadByUserName;
        @NotBlank
        private String uploadedBType;
        @NotBlank
        private String uploadedBTypeId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListResponse {
        private String id;
        private String fileName;
        private String documentUrl;
        private String fileType;
        private String uploadedByType;
        private String uploadedByTypeId;
        private String createdAt;
    }
}
