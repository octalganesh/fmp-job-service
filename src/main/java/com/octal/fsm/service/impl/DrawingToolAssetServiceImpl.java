package com.octal.fsm.service.impl;

import com.octal.fsm.dto.DrawingToolAssetDTO;
import com.octal.fsm.entities.DrawingToolAsset;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.repositories.DrawingToolAssetRepository;
import com.octal.fsm.service.DrawingToolAssetService;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DrawingToolAssetServiceImpl implements DrawingToolAssetService {

    @Autowired
    private DrawingToolAssetRepository drawingToolAssetRepository;

    @Override
    public void uploadFiles(DrawingToolAssetDTO.Add add) throws CodeException {
        if(add.getFileUrls().isEmpty()){
            throw new CodeException("No files to upload", null);
        }
        List<DrawingToolAsset> drawingToolAssets = new ArrayList<>();
        for(String fileUrl : add.getFileUrls()){
            DrawingToolAsset toolAsset = new DrawingToolAsset();
            toolAsset.setFileName(TextUtils.getFileNameFromFileUrl(fileUrl));
            toolAsset.setFileType(TextUtils.getFileTypeFromFileUrl(fileUrl));
            toolAsset.setFileUrl(fileUrl);
            drawingToolAssets.add(toolAsset);
        }
        drawingToolAssetRepository.saveAll(drawingToolAssets);
    }

    @Override
    public List<DrawingToolAssetDTO.ListResponse> getAllDrawingToolAssets() throws CodeException {
        List<DrawingToolAsset> drawingToolAssets = drawingToolAssetRepository.findAll();
        List<DrawingToolAssetDTO.ListResponse> responseList = new ArrayList<>();
        for(DrawingToolAsset asset : drawingToolAssets){
            DrawingToolAssetDTO.ListResponse response = new DrawingToolAssetDTO.ListResponse();
            response.setId(asset.getUuid());
            response.setFileName(asset.getFileName());
            response.setFileType(asset.getFileType());
            response.setFileUrl(asset.getFileUrl());
            response.setCreatedAt(asset.getCreatedAt().toString());
            response.setActive(asset.getActive());
            responseList.add(response);
        }
        return responseList;
    }
}
