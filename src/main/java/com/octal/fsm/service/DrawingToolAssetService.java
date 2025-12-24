package com.octal.fsm.service;

import com.octal.fsm.dto.DocumentDTO;
import com.octal.fsm.dto.DrawingToolAssetDTO;
import com.octal.fsm.exceptions.CodeException;

import java.util.List;

public interface DrawingToolAssetService {

    void uploadFiles(DrawingToolAssetDTO.Add add) throws CodeException;

    List<DrawingToolAssetDTO.ListResponse> getAllDrawingToolAssets() throws CodeException;
}
