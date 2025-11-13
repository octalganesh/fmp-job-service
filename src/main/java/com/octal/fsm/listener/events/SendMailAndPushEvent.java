package com.octal.fsm.listener.events;

import com.octal.fsm.dto.CustomerDTO;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PushNotificationRequest;
import com.octal.fsm.dto.TechnicianDTO;
import org.springframework.context.ApplicationEvent;

public class SendMailAndPushEvent extends ApplicationEvent {
    private final CustomerDTO.GetDetails customerDetails;
    private final JobDTO.Detail jobDetails;
    private final String loggedInuser;
    private final PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers;

    public SendMailAndPushEvent(CustomerDTO.GetDetails customerDetails, JobDTO.Detail jobDetails, String loggedInuser, PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers) {
        super(customerDetails);
        this.customerDetails = customerDetails;
        this.jobDetails = jobDetails;
        this.loggedInuser = loggedInuser;
        this.sendBulkNotificationToUsers = sendBulkNotificationToUsers;
    }

    public CustomerDTO.GetDetails getCustomerDetails() {
        return customerDetails;
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
