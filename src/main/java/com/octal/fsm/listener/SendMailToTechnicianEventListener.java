package com.octal.fsm.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.clients.TechnicianClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.dto.enums.PushNotificationType;
import com.octal.fsm.entities.JobTaskMappingTechnician;
import com.octal.fsm.listener.events.SendMailToTechnicianEvent;
import com.octal.fsm.service.EmailService;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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
    private TechnicianClient technicianClient;

    @Override
    @Async("sendMailToTechnicianEvent")
    public void onApplicationEvent(SendMailToTechnicianEvent event) {
        JobDTO.AssignJobToTechnician assignJobToTechnician = event.getAssignJobToTechnician();
        JobTaskMappingTechnician jobTaskMappingTech = event.getJobTaskMappingTech();
        processSendMailToTechnicianEvent(assignJobToTechnician,jobTaskMappingTech,event.getLoggedInuser(), event.getTenantId(), event.isSuperAdmin());
    }

    public void processSendMailToTechnicianEvent( JobDTO.AssignJobToTechnician assignJobToTechnician,JobTaskMappingTechnician jobTaskMappingTech, String loggedInUserEmail, Long tenantId, boolean isSuperAdmin) {
        ApiResponse technicianResponse = technicianClient.getTechnicianById(assignJobToTechnician.getTechnicianId(), loggedInUserEmail).getBody();
        if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200")) {
            JobDTO.Detail jobDetails = jobService.getJobById(assignJobToTechnician.getJobId(), loggedInUserEmail, tenantId, isSuperAdmin);
            Gson gson = new Gson();
            // Convert response data to TechnicianDTO.GetDetails
            if (jobDetails != null) {
                String jsonResponse = gson.toJson(technicianResponse.getData());
                TechnicianDTO.TechnicianData getDetails = gson.fromJson(jsonResponse, TechnicianDTO.TechnicianData.class);
                if (getDetails != null && getDetails.getEmail() != null) {
                    PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers = new PushNotificationRequest.SendBulkNotificationToUsers();
                    ResponseEntity<ApiResponse> notificationSlugContent = notificationClient.getNotificationContent(PushNotificationType.NEW_TASK_ASSIGNED.toString());
                    ApiResponse body = notificationSlugContent.getBody();
                    if (body != null) {
                        NotificationContentDTO.Request content = objectMapper.convertValue(body.getData(), NotificationContentDTO.Request.class);
                        content.setMessage(TextUtils.replacePlaceholderInMessage(content.getMessage(), "#technicianName", getDetails.getName()));
                        sendBulkNotificationToUsers.setTitle(content.getTitle());
                        sendBulkNotificationToUsers.setBody(content.getMessage());
                        sendBulkNotificationToUsers.setType(PushNotificationType.NEW_TASK_ASSIGNED);
                        sendBulkNotificationToUsers.setTypeId(jobTaskMappingTech.getUuid());
                        Set<MultiUserDeviceDetailsDTO> set = new HashSet<>();
                        MultiUserDeviceDetailsDTO multiUserDeviceDetailsDTO = new MultiUserDeviceDetailsDTO();
                        multiUserDeviceDetailsDTO.setDeviceToken(getDetails.getMultiUserDeviceDetails().getDeviceToken());
                        multiUserDeviceDetailsDTO.setDeviceType(getDetails.getMultiUserDeviceDetails().getDeviceType());
                        multiUserDeviceDetailsDTO.setUserId(getDetails.getId());
                        set.add(multiUserDeviceDetailsDTO);
                        sendBulkNotificationToUsers.setTechnicianFcmTokenList(set);
                        sendBulkNotificationToUsers.setFrontOfficeFcmTokenList(new HashSet<>());
                    }
                    sendNotificationToUser(sendBulkNotificationToUsers);
                }
                sendJobEmailToTechnician(getDetails, jobDetails, loggedInUserEmail,tenantId, isSuperAdmin);
            }
        }

    }

    private void sendNotificationToUser(PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers) {
        notificationClient.sendBulkPushNotification(sendBulkNotificationToUsers);
    }

    private void sendJobEmailToTechnician(TechnicianDTO.TechnicianData technician, JobDTO.Detail jobDetails, String loggedInuser, Long tenantId, boolean isSuperAdmin) {
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