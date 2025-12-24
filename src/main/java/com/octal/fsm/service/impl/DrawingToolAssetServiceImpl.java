package com.octal.fsm.service.impl;

import com.octal.fsm.dto.DrawingToolAssetDTO;
import com.octal.fsm.entities.AssetItem;
import com.octal.fsm.entities.DrawingToolAsset;
import com.octal.fsm.entities.enums.Category;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.repositories.DrawingToolAssetRepository;
import com.octal.fsm.service.DrawingToolAssetService;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DrawingToolAssetServiceImpl implements DrawingToolAssetService {

    @Autowired
    private DrawingToolAssetRepository drawingToolAssetRepository;

    @Override
    public void uploadFiles(DrawingToolAssetDTO.Add add) throws CodeException {
        DrawingToolAsset category = new DrawingToolAsset();
        category.setName(Category.valueOf(add.getCategoryName()));
        List<AssetItem> assetItems =
                add.getAssets()
                        .stream()
                        .map(dto -> toAssetEntity(dto, category))
                        .collect(Collectors.toList());

        category.setAssets(assetItems);
        drawingToolAssetRepository.save(category);
    }

    private AssetItem toAssetEntity(DrawingToolAssetDTO.AssetItemAddRequestDTO dto, DrawingToolAsset category) {
        AssetItem item = new AssetItem();
        item.setName(TextUtils.getFileNameFromFileUrl(dto.getImageUrl()));
        item.setFileType(TextUtils.getFileTypeFromFileUrl(dto.getImageUrl()));
        item.setImageUrl(dto.getImageUrl());
        item.setWidth(dto.getWidth() != null ? dto.getWidth() : 100f);
        item.setHeight(dto.getHeight() != null ? dto.getHeight() : 100f);
        item.setCategory(category);
        return item;
    }

    @Override
    public List<DrawingToolAssetDTO.ListResponse> getAllDrawingToolAssets() throws CodeException {
        return drawingToolAssetRepository.findAll()
                .stream().map(this::toCategoryDto).collect(Collectors.toList());
    }

    private DrawingToolAssetDTO.ListResponse toCategoryDto(DrawingToolAsset entity) {
        DrawingToolAssetDTO.ListResponse dto = new DrawingToolAssetDTO.ListResponse();
        dto.setCategory_name(entity.getName().name());
        List<DrawingToolAssetDTO.AssetItemResponseDTO> assetDtos =
                entity.getAssets()
                        .stream()
                        .map(this::toAssetDto)
                        .collect(Collectors.toList());
        dto.setAssets(assetDtos);
        return dto;
    }

    private DrawingToolAssetDTO.AssetItemResponseDTO toAssetDto(AssetItem item) {
        DrawingToolAssetDTO.AssetItemResponseDTO dto = new DrawingToolAssetDTO.AssetItemResponseDTO();
        dto.setId(item.getUuid());
        dto.setName(item.getName());
        dto.setImageUrl(item.getImageUrl());
        dto.setFileType(item.getFileType());
        dto.setWidth(item.getWidth());
        dto.setHeight(item.getHeight());
        return dto;
    }
}
