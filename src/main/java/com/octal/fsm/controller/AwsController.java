package com.octal.fsm.controller;


import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.AwsDTO;
import com.octal.fsm.service.S3PresignedUrlService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

public class AwsController {
    private static final Logger logger = LogManager.getLogger(AwsController.class);
    @Autowired
    private S3PresignedUrlService s3PresignedUrlService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse> getPresignedUrl(@RequestBody AwsDTO.GetPreSignedUrlRequest getPreSignedUrlRequest, HttpServletRequest httpServletRequest) {
        logger.info("AdminAuthController.getPresignedUrl");
        try {
            String url = s3PresignedUrlService.generatePresignedUrl(getPreSignedUrlRequest.getPath(), getPreSignedUrlRequest.getContentType());
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Presigned URL Generated Successfully.", url,
                    "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(Boolean.FALSE, e.getMessage(), null,
                    "101", HttpStatus.OK), HttpStatus.OK);
        }
    }
}
