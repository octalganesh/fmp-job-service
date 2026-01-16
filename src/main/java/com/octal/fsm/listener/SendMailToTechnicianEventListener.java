package com.octal.fsm.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.clients.TechnicianClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.dto.enums.PushNotificationType;
import com.octal.fsm.entities.JobMappingTask;
import com.octal.fsm.entities.JobTaskMappingTechnician;
import com.octal.fsm.listener.events.SendMailToTechnicianEvent;
import com.octal.fsm.repositories.JobMappingTaskRepository;
import com.octal.fsm.service.EmailService;
import com.octal.fsm.service.JobService;
import com.octal.fsm.service.NotificationClientService;
import com.octal.fsm.service.TechnicianClientService;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class SendMailToTechnicianEventListener implements ApplicationListener<SendMailToTechnicianEvent> {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TechnicianClientService technicianClientService;

    @Autowired
    private NotificationClientService notificationClientService;

    @Override
    @Async("sendMailToTechnicianEvent")
    public void onApplicationEvent(SendMailToTechnicianEvent event) {
        JobDTO.AssignJobToTechnician assignJobToTechnician = event.getAssignJobToTechnician();
        JobTaskMappingTechnician jobTaskMappingTech = event.getJobTaskMappingTech();
        processSendMailToTechnicianEvent(assignJobToTechnician,jobTaskMappingTech,event.getLoggedInuser(), event.getTenantId(), event.isSuperAdmin(), assignJobToTechnician.getTaskShowId());
    }

    public void processSendMailToTechnicianEvent(JobDTO.AssignJobToTechnician assignJobToTechnician, JobTaskMappingTechnician jobTaskMappingTech, String loggedInUserEmail, Long tenantId, boolean isSuperAdmin, String taskShowId) {
        try {
            TechnicianDTO.GetDetails getDetails = technicianClientService.getTechnicianById(assignJobToTechnician.getTechnicianId(), loggedInUserEmail);
            if (getDetails != null) {
                JobDTO.Detail jobDetails = jobService.getJobById(assignJobToTechnician.getJobId(), loggedInUserEmail, tenantId, isSuperAdmin);
                if (jobDetails != null) {
                    NotificationContentDTO.Request notificationContent = notificationClientService.getNotificationContent(PushNotificationType.NEW_TASK_ASSIGNED.toString());
                    if (notificationContent != null) {
                        PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers = new PushNotificationRequest.SendBulkNotificationToUsers();
                        sendBulkNotificationToUsers.setTitle(notificationContent.getTitle());
                        sendBulkNotificationToUsers.setBody(TextUtils.replacePlaceholderInMessage(notificationContent.getMessage(), "#technicianName", getDetails.getName()));
                        sendBulkNotificationToUsers.setType(PushNotificationType.NEW_TASK_ASSIGNED);
                        sendBulkNotificationToUsers.setTypeId(jobTaskMappingTech.getUuid());
                        sendBulkNotificationToUsers.setTaskId(jobTaskMappingTech.getUuid());
                        sendBulkNotificationToUsers.setTaskShowId(assignJobToTechnician.getTaskShowId());
                        sendBulkNotificationToUsers.setTaskName(assignJobToTechnician.getTaskName());
                        if(getDetails.getMultiUserDeviceDetails() != null){
                            MultiUserDeviceDetailsDTO multiUserDeviceDetailsDTO = new MultiUserDeviceDetailsDTO();
                            multiUserDeviceDetailsDTO.setDeviceToken(getDetails.getMultiUserDeviceDetails().getDeviceToken());
                            multiUserDeviceDetailsDTO.setDeviceType(getDetails.getMultiUserDeviceDetails().getDeviceType());
                            multiUserDeviceDetailsDTO.setUserId(getDetails.getId());
                            multiUserDeviceDetailsDTO.setPushEnabled(getDetails.getMultiUserDeviceDetails().getPushEnabled() != null ? getDetails.getMultiUserDeviceDetails().getPushEnabled() : true);
                            Set<MultiUserDeviceDetailsDTO> set = new HashSet<>();
                            set.add(multiUserDeviceDetailsDTO);
                            sendBulkNotificationToUsers.setTechnicianFcmTokenList(set);
                        }
                        sendBulkNotificationToUsers.setFrontOfficeFcmTokenList(new HashSet<>());
                        sendNotificationToUser(sendBulkNotificationToUsers);
                    }
                    if (getDetails.getEmail() != null) {
                        sendJobEmailToTechnician(getDetails, jobDetails, loggedInUserEmail, tenantId, isSuperAdmin);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationToUser(PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers) {
        notificationClient.sendBulkPushNotification(sendBulkNotificationToUsers);
    }

    private void sendJobEmailToTechnician(TechnicianDTO.GetDetails technician, JobDTO.Detail jobDetails, String loggedInuser, Long tenantId, boolean isSuperAdmin) {
        try {
            // ✅ Prepare dynamic placeholders
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("technicianName", technician.getName());
            placeholders.put("jobId", jobDetails.getJobId());
            placeholders.put("jobType", jobDetails.getJobType());
            placeholders.put("customerName", jobDetails.getCustomerDetails().getCustomerName());
            placeholders.put("serviceLocation", jobDetails.getServiceLocation());
            placeholders.put("startDate", jobDetails.getJobStartDate());
            placeholders.put("endDate", jobDetails.getJobEndDate());
            placeholders.put("logoUrl", "https://yourcdn.com/logo.png");

            // ✅ Prepare email data
            EmailDTO mail = new EmailDTO();
            mail.setMailTo(technician.getEmail());
            mail.setTemplateName("TECHNICIAN_JOB_ASSIGNED");
            mail.setProps(placeholders);

            // ✅ Send email using your unified sendMail method
            emailService.sendMail(mail, loggedInuser, tenantId, isSuperAdmin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}