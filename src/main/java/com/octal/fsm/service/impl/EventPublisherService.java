package com.octal.fsm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octal.fsm.dto.SystemEventLogDTO;
import com.octal.fsm.entities.SystemEventLog;
import com.octal.fsm.repositories.SystemEventLogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherService {

    private final SystemEventLogRepository systemEventLogRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisherService(SystemEventLogRepository systemEventLogRepository,
                                 ApplicationEventPublisher applicationEventPublisher) {
        this.systemEventLogRepository = systemEventLogRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(SystemEventLogDTO event) {
        // save to DB
        SystemEventLog log = new SystemEventLog();
        log.setEventType(event.getEventType());
        log.setDescription(event.getDescription());
        log.setReferenceId(event.getReferenceId());
        log.setPerformedBy(event.getPerformedBy());
        log.setProfileUrl(event.getProfileUrl());
        systemEventLogRepository.save(log);

        applicationEventPublisher.publishEvent(event);
    }
}
