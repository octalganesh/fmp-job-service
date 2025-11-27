package com.octal.fsm.listener.events;

import com.octal.fsm.dto.CustomerDTO;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PushNotificationRequest;
import com.octal.fsm.entities.JobTaskMappingTechnician;
import org.springframework.context.ApplicationEvent;

public class SendMailAndPushEvent extends ApplicationEvent {
    private final JobTaskMappingTechnician taskMappingTechnician;
    private final Long tenantId;
    private final String loggedInuser;

    public SendMailAndPushEvent(JobTaskMappingTechnician taskMappingTechnician, Long tenantId, String loggedInuser) {
        super(taskMappingTechnician);
        this.taskMappingTechnician = taskMappingTechnician;
        this.tenantId = tenantId;
        this.loggedInuser = loggedInuser;
    }

    public String getLoggedInuser() {
        return loggedInuser;
    }

    public JobTaskMappingTechnician getTaskMappingTechnician() {
        return taskMappingTechnician;
    }

    public Long getTenantId() {
        return tenantId;
    }
}
