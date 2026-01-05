package com.octal.fsm.service.impl;

import com.octal.fsm.dto.SystemEventLogDTO;
import com.octal.fsm.entities.SystemEventLog;
import com.octal.fsm.repositories.SystemEventLogRepository;
import com.octal.fsm.service.GeneralSettingService;
import com.octal.fsm.service.SystemEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemEventServiceImpl implements SystemEventService {

    @Autowired
    private SystemEventLogRepository systemEventLogRepository;

    @Autowired
    private GeneralSettingService generalSettingService;

    @Override
    public List<SystemEventLogDTO> getEventsForReference(String referenceId,  Long tenantId) {
        List<SystemEventLog> byReferenceIdOrderByCreatedAtDesc = systemEventLogRepository.findByReferenceIdOrderByCreatedAtDesc(referenceId);
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
        return byReferenceIdOrderByCreatedAtDesc.stream().map(f -> {
            SystemEventLogDTO dto = new SystemEventLogDTO();
            dto.setEventType(f.getEventType());
            dto.setDescription(f.getDescription());
            dto.setReferenceId(f.getReferenceId());
            dto.setPerformedBy(f.getPerformedBy());
            dto.setCreatedAt(f.getCreatedAt().format(dateTimeFormatter));
            dto.setProfileUrl(f.getProfileUrl());
            return dto;
        }).collect(Collectors.toList());
    }
}
