package com.octal.fsm.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.dto.enums.PushNotificationType;
import com.octal.fsm.entities.Appointment;
import com.octal.fsm.listener.events.AppointmentNotificationEvent;
import com.octal.fsm.service.JobService;
import com.octal.fsm.service.NotificationClientService;
import com.octal.fsm.service.TechnicianClientService;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AppointmentNotificationEventListener implements ApplicationListener<AppointmentNotificationEvent> {
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
    @Async("appointmentNotificationToTechnicianEvent")
    public void onApplicationEvent(AppointmentNotificationEvent appointmentNotificationEvent) {
        if (appointmentNotificationEvent.getAppointment() == null) {
            return;
        }
        processSendMailToTechnicianEvent(appointmentNotificationEvent.getAppointment(), appointmentNotificationEvent.getLoggedInuser());
    }

    public void processSendMailToTechnicianEvent(Appointment appointment, String loggedInUser) {
        try {
            if (appointment.getTechnicianId() != null) {
                TechnicianDTO.GetDetails getDetails = technicianClientService.getTechnicianById(appointment.getTechnicianId(), loggedInUser);
                if (getDetails == null) {
                    return;
                }
                NotificationContentDTO.Request notificationContent = notificationClientService.getNotificationContent(PushNotificationType.NEW_APPOINTMENT_ASSIGNED.toString());
                if (notificationContent != null) {
                    PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers = new PushNotificationRequest.SendBulkNotificationToUsers();
                    sendBulkNotificationToUsers.setTitle(notificationContent.getTitle());
                    String message = notificationContent.getMessage();
                    message = TextUtils.replacePlaceholderInMessage(message, "#technicianName", getDetails.getName());
                    message = TextUtils.replacePlaceholderInMessage(message, "#taskId", appointment.getJobTaskId());
                    sendBulkNotificationToUsers.setBody(message);
                    sendBulkNotificationToUsers.setType(PushNotificationType.NEW_APPOINTMENT_ASSIGNED);
                    sendBulkNotificationToUsers.setTypeId(appointment.getUuid());
                    sendBulkNotificationToUsers.setTaskId(appointment.getJobTaskId());
                    sendBulkNotificationToUsers.setTaskShowId(appointment.getJobTaskId());
                    sendBulkNotificationToUsers.setTaskName(appointment.getJobTaskId());
                    if (getDetails.getMultiUserDeviceDetails() != null) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationToUser(PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToUsers) {
        notificationClient.sendBulkPushNotification(sendBulkNotificationToUsers);
    }
}
