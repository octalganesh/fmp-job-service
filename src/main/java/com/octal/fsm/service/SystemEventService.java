package com.octal.fsm.service;

import com.octal.fsm.dto.SystemEventLogDTO;
import com.octal.fsm.entities.SystemEventLog;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SystemEventService {

    public List<SystemEventLogDTO> getEventsForReference(String referenceId, Long tenantId);
}
