package com.octal.fsm.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octal.fsm.clients.*;
import com.octal.fsm.dto.*;
import com.octal.fsm.dto.enums.PushNotificationType;
import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobMappingTask;
import com.octal.fsm.entities.JobTaskMappingTechnician;
import com.octal.fsm.entities.MultiUserDeviceDetails;
import com.octal.fsm.listener.events.SendMailAndPushEvent;
import com.octal.fsm.repositories.JobMappingTaskRepository;
import com.octal.fsm.service.AdminClientService;
import com.octal.fsm.service.EmailService;
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
public class SendMailAndPushEventListener implements ApplicationListener<SendMailAndPushEvent> {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private com.octal.fsm.service.JobService jobService;

    @Autowired
    private AdminClient adminClient;
    @Autowired
    private TechnicianClient technicianClient;
    @Autowired
    private JobMappingTaskRepository jobMappingTaskRepository;

    @Autowired
    private TechnicianClientService technicianClientService;

    @Autowired
    private AdminClientService adminClientService;

    @Autowired
    private NotificationClientService notificationClientService;

    @Override
    @Async("sendMailAndPushEvent")
    public void onApplicationEvent(SendMailAndPushEvent event) {

        JobTaskMappingTechnician taskMappingTechnician = event.getTaskMappingTechnician();
        processData(taskMappingTechnician, event.getTenantId(), event.getLoggedInuser());
    }

    private void processData(JobTaskMappingTechnician jobTaskMappingTechnician, Long tenantId, String userName) {
        try {
            NotificationContentDTO.Request notificationContent = notificationClientService.getNotificationContent(PushNotificationType.TASK_STATUS_CHANGE.toString());
            if (notificationContent != null) {
                PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToFront = new PushNotificationRequest.SendBulkNotificationToUsers();
                TechnicianDTO.GetDetails technicianDetails = technicianClientService.getTechnicianById(jobTaskMappingTechnician.getTechnicianId(), userName);
                if(technicianDetails != null){
                    notificationContent.setMessage(TextUtils.replacePlaceholderInMessage(notificationContent.getMessage(), "#technicianName", technicianDetails.getName()));
                }
                sendBulkNotificationToFront.setTitle(notificationContent.getTitle());
                sendBulkNotificationToFront.setType(PushNotificationType.TASK_STATUS_CHANGE);
                Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(jobTaskMappingTechnician.getJobTaskMappingId());
                CustomerDTO.GetDetails customerDetails = new CustomerDTO.GetDetails();
                JobDTO.Detail jobDetails = new JobDTO.Detail();
                if (jobMappingTask.isPresent()) {
                    sendBulkNotificationToFront.setTypeId(jobMappingTask.get().getJob().getJobTypeId());
                    String customerId = jobMappingTask.get().getJob().getCustomerId();
                    Job job = jobMappingTask.get().getJob();

                    jobDetails.setJobId(job.getJobId());
                    jobDetails.setJobTypeId(job.getJobTypeId());
                    jobDetails.setCustomerTypeId(job.getCustomerTypeId());
                    jobDetails.setLeadSourceId(job.getLeadSourceId());
                    jobDetails.setJobDescription(job.getJobDescription());
                    jobDetails.setAdditionalNotes(job.getAdditionalNotes());
                    jobDetails.setJobStatus(job.getJobStatus());
                    jobDetails.setServiceLocation(job.getServiceLocation());
                    jobDetails.setServiceLocationLat(job.getServiceLocationLat());
                    jobDetails.setServiceLocationLng(job.getServiceLocationLng());
                    jobDetails.setJobStartDate(job.getJobStartDate().toString());
                    if(job.getJobEndDate() != null){
                        jobDetails.setJobEndDate(job.getJobEndDate().toString());
                    }
                    CustomerDTO.GetDetails customerById = adminClientService.getCustomerById(customerId);
                    if(customerById != null){
                        customerDetails = customerById;
                    }
                    notificationContent.setMessage(TextUtils.replacePlaceholderInMessage(notificationContent.getMessage(), "#status", jobMappingTask.get().getJobTaskStatus()));
                    notificationContent.setMessage(TextUtils.replacePlaceholderInMessage(notificationContent.getMessage(), "#jobId", job.getJobId()));
                    sendBulkNotificationToFront.setTitle(TextUtils.replacePlaceholderInMessage(notificationContent.getTitle(), "#jobID", job.getJobId()));
                }
                ApiResponse frontOfficeDevices = jobService.getFrontOfficeDevices(null, tenantId).getBody();
                Set<MultiUserDeviceDetails> frontOfficeDeviceDetails = new HashSet<>();
                if (frontOfficeDevices != null) {
                    List<MultiUserDeviceDetails> frontOfficedeviceList = objectMapper.convertValue(frontOfficeDevices.getData(), new TypeReference<List<MultiUserDeviceDetails>>() {
                    });
                    if (frontOfficedeviceList != null && !frontOfficedeviceList.isEmpty()) {
                        for (MultiUserDeviceDetails multiUserDeviceDetails : frontOfficedeviceList) {
                            MultiUserDeviceDetails dto = new MultiUserDeviceDetails();
                            dto.setDeviceToken(multiUserDeviceDetails.getDeviceToken());
                            dto.setDeviceType(multiUserDeviceDetails.getDeviceType());
                            dto.setAppVersion(multiUserDeviceDetails.getAppVersion());
                            dto.setDeviceId(multiUserDeviceDetails.getDeviceId());
                            dto.setPushEnabled(multiUserDeviceDetails.getPushEnabled() != null ? multiUserDeviceDetails.getPushEnabled() : true);
                            frontOfficeDeviceDetails.add(dto);
                        }
                    }
                    sendBulkNotificationToFront.setBody(notificationContent.getMessage());
                    sendBulkNotificationToFront.setTechnicianFcmTokenList(new HashSet<>());
                    sendBulkNotificationToFront.setFrontOfficeFcmTokenList(frontOfficeDeviceDetails);

                    notificationClient.sendBulkPushNotification(sendBulkNotificationToFront);
                }
                sendJobEmailToTechnician(customerDetails, jobDetails, userName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            emailService.sendMail(mail, loggedInuser, 1l, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}