package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Lob;
import java.util.List;

@Data
public class AppointmentTypeDTO {

    @Data
    @AllArgsConstructor
    public static class Add {
        private String id;
        private String name;
        private String description;
        private List<String> jobTypeIds;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {
        private String id;
        private String name;
        private String createdAt;
        private String updatedAt;
        private Boolean isActive;
        private String description;
        private List<String> jobTypeIds;
    }
}
