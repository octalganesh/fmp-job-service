package com.octal.fsm.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class NotificationContentDTO {

    @Data
    public static class Response {
        private String id;
        private String title;
        private String message;
        private String slug;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    public static class Request {
        private String id;
        private String title;
        private String message;
        private String slug;
    }

    @Data
    public static class ListRequest {
        private int pageNumber = 0;
        private int pageSize = 5;
        private String searchText;
        private String shortingField = "createdAt";
        private Boolean asc = true;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;
        private Boolean isActive;
        private String sortBy;
    }


}