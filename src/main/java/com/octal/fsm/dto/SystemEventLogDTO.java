package com.octal.fsm.dto;

import com.octal.fsm.entities.enums.SystemEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemEventLogDTO {

    private SystemEventType eventType;
    private String description;
    private String referenceId;   // jobId, invoiceId, taskId etc.
    private String performedBy;   // username/email
    private String createdAt;
    private String profileUrl;
}
