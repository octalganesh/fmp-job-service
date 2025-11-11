package com.octal.fsm.listener.events;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PushNotificationRequest;
import com.octal.fsm.dto.TechnicianDTO;
import org.springframework.context.ApplicationEvent;

public class SendMailToTechnicianEvent extends ApplicationEvent {
    private final TechnicianDTO.TechnicianData technicianDTO;
    private final JobDTO.Detail jobDetails;
    private final String loggedInuser;
    private final PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers;

    public SendMailToTechnicianEvent(TechnicianDTO.TechnicianData technicianDTO, JobDTO.Detail jobDetails, String loggedInuser, PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers) {
        super(technicianDTO);
        this.technicianDTO = technicianDTO;
        this.jobDetails = jobDetails;
        this.loggedInuser = loggedInuser;
        this.sendBulkNotificationToUsers = sendBulkNotificationToUsers;
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
