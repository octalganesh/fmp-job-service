package com.octal.fsm.dto;

import com.octal.fsm.entities.AssetItem;
import com.octal.fsm.entities.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class DrawingToolAssetDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {
        private String categoryName; // GATES, PANELS, POOL, BUSHES
        private List<AssetItemAddRequestDTO> assets;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListResponse {
        private String categoryName;
        private List<AssetItemResponseDTO> assets;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssetItemResponseDTO {
        private String id;
        private String name;
        private String imageUrl;
        private String fileType;
        private Float width;
        private Float height;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssetItemAddRequestDTO  {
        private String name;
        private String imageUrl;
        private String fileType;
        private Float width;
        private Float height;
    }
}
