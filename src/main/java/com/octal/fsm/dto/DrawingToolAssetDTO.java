package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class DrawingToolAssetDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {

        @NotBlank
        private List<String> fileUrls;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListResponse {
        private String id;
        private String fileName;
        private String fileUrl;
        private String fileType;
        private String createdAt;
        private boolean isActive;
    }
}
