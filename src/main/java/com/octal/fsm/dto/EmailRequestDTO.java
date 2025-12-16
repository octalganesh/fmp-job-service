package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailRequestDTO {

    private String from;
    private String toMail;
    private String subject;
    private String body;
    private List<String> attachments;
}
