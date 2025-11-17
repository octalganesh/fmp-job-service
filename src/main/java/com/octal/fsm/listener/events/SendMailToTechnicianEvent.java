package com.octal.fsm.listener.events;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PushNotificationRequest;
import com.octal.fsm.dto.TechnicianDTO;
import org.springframework.context.ApplicationEvent;

public class SendMailToTechnicianEvent extends ApplicationEvent {
    private final TechnicianDTO.TechnicianData technicianDTO;
    private final JobDTO.Detail jobDetails;
    private final String loggedInuser;
    private final Long tenantId;
    private final boolean isSuperAdmin;
    private final PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers;


    public SendMailToTechnicianEvent(TechnicianDTO.TechnicianData technicianDTO, JobDTO.Detail jobDetails, String loggedInuser, Long tenantId, boolean isSuperAdmin, PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers) {
        super(technicianDTO);
        this.technicianDTO = technicianDTO;
        this.jobDetails = jobDetails;
        this.loggedInuser = loggedInuser;
        this.tenantId = tenantId;
        this.isSuperAdmin = isSuperAdmin;
        this.sendBulkNotificationToUsers = sendBulkNotificationToUsers;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public TechnicianDTO.TechnicianData getTechnicianDTO() {
        return technicianDTO;
    }

    public JobDTO.Detail getJobDetails() {
        return jobDetails;
    }

    public PushNotificationRequest.SendBulkNotificationToUsers getSendBulkNotificationToUsers() {
        return sendBulkNotificationToUsers;
    }

    public String getLoggedInuser() {
        return loggedInuser;
    }
}
