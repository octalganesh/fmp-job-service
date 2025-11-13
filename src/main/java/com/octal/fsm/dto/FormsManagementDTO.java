package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class FormsManagementDTO {

    @Data
    public static class Add {
        private String id;
        @NotNull(message = "Form title is required")
        private String title;
        private String description;
        private String category;
        private String content;
        @NotEmpty(message = "Please select at least one job type")
        private List<String> jobType;
        private List<String> jobTypesNames;
        private Boolean isActive;
    }

    @Data
    public static class Detail {
        private String id;
        private String title;
        private String description;
        private String category;
        private String content;
        private List<String> jobType;
        private List<String> jobTypeNames;
        private Boolean active;
        private String createdAt;
        private String updatedAt;
    }

}
