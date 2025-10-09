package com.octal.fsm.dto;

import lombok.Data;

public class AwsDTO {

    @Data
    public static class GetPreSignedUrlRequest {
        private String contentType;
        private String path;
    }
}
