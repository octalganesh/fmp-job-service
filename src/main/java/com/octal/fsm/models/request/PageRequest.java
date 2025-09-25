package com.octal.fsm.models.request;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class PageRequest {
    private PageRequest() {
        //private default constructor
    }

    @Data
    public static class Add {
        private String id;
        private String name;
        private String description;
        private Boolean active;
    }

    @Data
    public static class List {
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
        private String assignedLeads;
        private String joinDate;
        private String designation;
    }


}
