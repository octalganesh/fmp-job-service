package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.DrawingToolAssetDTO;
import com.octal.fsm.service.DrawingToolAssetService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Repository
@RequestMapping("drawing-tool-assets")
public class DrawingToolAssetController extends BaseController{

    private static final Logger logger = LogManager.getLogger(DrawingToolAssetController.class);

    @Autowired
    private DrawingToolAssetService drawingToolAssetService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadFiles(@RequestBody DrawingToolAssetDTO.Add add, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            drawingToolAssetService.uploadFiles(add);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Drawing assets Upload Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error uploading drawing files: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/all-files")
    public ResponseEntity<ApiResponse> getAll(HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Drawing assets Fetch Successfully.", drawingToolAssetService.getAllDrawingToolAssets(), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error getting drawing files: {}", e.getMessage(), e);
            return handleException(e);
        }
    }



}
