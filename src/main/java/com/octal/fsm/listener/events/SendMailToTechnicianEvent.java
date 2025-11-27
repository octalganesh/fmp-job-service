package com.octal.fsm.listener.events;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PushNotificationRequest;
import com.octal.fsm.dto.TechnicianDTO;
import com.octal.fsm.entities.JobTaskMappingTechnician;
import org.springframework.context.ApplicationEvent;

public class SendMailToTechnicianEvent extends ApplicationEvent {
    private final JobDTO.AssignJobToTechnician assignJobToTechnician;
    private final JobTaskMappingTechnician jobTaskMappingTech;
    private final String loggedInuser;
    private final Long tenantId;
    private final boolean isSuperAdmin;


    public SendMailToTechnicianEvent(JobDTO.AssignJobToTechnician assignJobToTechnician,JobTaskMappingTechnician jobTaskMappingTech, String loggedInuser, Long tenantId, boolean isSuperAdmin) {
        super(assignJobToTechnician);
        this.assignJobToTechnician = assignJobToTechnician;
        this.jobTaskMappingTech = jobTaskMappingTech;
        this.loggedInuser = loggedInuser;
        this.tenantId = tenantId;
        this.isSuperAdmin = isSuperAdmin;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public JobDTO.AssignJobToTechnician getAssignJobToTechnician() {
        return assignJobToTechnician;
    }

    public JobTaskMappingTechnician getJobTaskMappingTech() {
        return jobTaskMappingTech;
    }

    public String getLoggedInuser() {
        return loggedInuser;
    }
}
