package com.octal.fsm.listener;

import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.listener.events.SendMailAndPushEvent;
import com.octal.fsm.listener.events.SendMailToTechnicianEvent;
import com.octal.fsm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class SendMailAndPushEventListener implements ApplicationListener<SendMailAndPushEvent> {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationClient notificationClient;

    @Override
    @Async("sendMailAndPushEvent")
    public void onApplicationEvent(SendMailAndPushEvent event) {
        CustomerDTO.GetDetails customerDetails = event.getCustomerDetails();
        JobDTO.Detail jobDetails = event.getJobDetails();

        sendJobEmailToTechnician(customerDetails, jobDetails, event.getLoggedInuser());

        sendNotificationToUser(event.getSendBulkNotificationToUsers());
    }

    private void sendNotificationToUser(PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers){
        notificationClient.sendBulkPushNotification(sendBulkNotificationToUsers);
    }

    private void sendJobEmailToTechnician(CustomerDTO.GetDetails customerDetails, JobDTO.Detail jobDetails, String loggedInuser) {
        try {
            // ✅ Prepare dynamic placeholders
            Map<String, Object> placeholders = new HashMap<>();
//            placeholders.put("technicianName", customerDetails.getName());
            placeholders.put("jobId", jobDetails.getJobId());
            placeholders.put("jobType", jobDetails.getJobType());
            placeholders.put("customerName", customerDetails.getName());
            placeholders.put("serviceLocation", jobDetails.getServiceLocation());
            placeholders.put("startDate", jobDetails.getJobStartDate());
            placeholders.put("endDate", jobDetails.getJobEndDate());
            placeholders.put("logoUrl", "https://yourcdn.com/logo.png");

            // ✅ Prepare email data
            EmailDTO mail = new EmailDTO();
            mail.setMailTo(customerDetails.getEmail());
            mail.setTemplateName("TASK_STATUS_CHANGE");
            mail.setProps(placeholders);

            // ✅ Send email using your unified sendMail method
            emailService.sendMail(mail, loggedInuser);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}