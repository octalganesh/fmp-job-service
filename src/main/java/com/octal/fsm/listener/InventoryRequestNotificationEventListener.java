package com.octal.fsm.listener;

import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.dto.MultiUserDeviceDetailsDTO;
import com.octal.fsm.dto.NotificationContentDTO;
import com.octal.fsm.dto.PushNotificationRequest;
import com.octal.fsm.dto.TechnicianDTO;
import com.octal.fsm.dto.enums.PushNotificationType;
import com.octal.fsm.entities.InventoryRequest;
import com.octal.fsm.listener.events.InventoryRequestNotificationEvent;
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
public class InventoryRequestNotificationEventListener implements ApplicationListener<InventoryRequestNotificationEvent> {

    @Autowired
    private TechnicianClientService technicianClientService;
    @Autowired
    private NotificationClientService notificationClientService;

    @Autowired
    private NotificationClient notificationClient;

    @Override
    @Async("sendMailAndPushEventInventory")
    public void onApplicationEvent(InventoryRequestNotificationEvent inventoryRequestNotificationEvent) {
        processData(inventoryRequestNotificationEvent.getInventoryRequest(), inventoryRequestNotificationEvent.getTenantId(), inventoryRequestNotificationEvent.getLoggedInuser());
    }

    private void processData(InventoryRequest inventoryRequest, Long tenantId, String userName) {
        try {
            NotificationContentDTO.Request notificationContent = null;
            PushNotificationRequest.SendBulkNotificationToUsers sendBulkNotificationToTech = new PushNotificationRequest.SendBulkNotificationToUsers();
            if (inventoryRequest.getStatus().equalsIgnoreCase("APPROVED")) {
                notificationContent = notificationClientService.getNotificationContent(PushNotificationType.INVENTORY_REQUEST_APPROVED.toString());
                sendBulkNotificationToTech.setType(PushNotificationType.INVENTORY_REQUEST_APPROVED);
            } else if (inventoryRequest.getStatus().equalsIgnoreCase("CANCELLED")) {
                notificationContent = notificationClientService.getNotificationContent(PushNotificationType.INVENTORY_REQUEST_CANCELLED.toString());
                sendBulkNotificationToTech.setType(PushNotificationType.INVENTORY_REQUEST_CANCELLED);
            }
            if (notificationContent != null) {
                TechnicianDTO.GetDetails technicianDetails = technicianClientService.getTechnicianById(inventoryRequest.getTechnicianId(), userName);
                if (technicianDetails == null || technicianDetails.getMultiUserDeviceDetails() == null || technicianDetails.getMultiUserDeviceDetails().getDeviceToken() == null) {
                    return;
                }
                String message = notificationContent.getMessage();
                message = TextUtils.replacePlaceholderInMessage(message, "#technicianName", technicianDetails.getName());
                message = TextUtils.replacePlaceholderInMessage(message, "#reqShowId", inventoryRequest.getRequestShowId());
                if ("CANCELLED".equalsIgnoreCase(inventoryRequest.getStatus()) && inventoryRequest.getRejectionReason() != null) {
                    message = TextUtils.replacePlaceholderInMessage(message, "#reason", inventoryRequest.getRejectionReason());
                }

                sendBulkNotificationToTech.setTitle(notificationContent.getTitle());
                sendBulkNotificationToTech.setBody(message);
                sendBulkNotificationToTech.setTypeId(inventoryRequest.getUuid());
                sendBulkNotificationToTech.setTaskId(inventoryRequest.getTaskId());
                sendBulkNotificationToTech.setTaskShowId(inventoryRequest.getRequestShowId());

                MultiUserDeviceDetailsDTO device = new MultiUserDeviceDetailsDTO();
                device.setDeviceToken(technicianDetails.getMultiUserDeviceDetails().getDeviceToken());
                device.setDeviceType(technicianDetails.getMultiUserDeviceDetails().getDeviceType());
                device.setUserId(technicianDetails.getId());
                Set<MultiUserDeviceDetailsDTO> techDevices = new HashSet<>();
                techDevices.add(device);

                sendBulkNotificationToTech.setTechnicianFcmTokenList(techDevices);
                sendBulkNotificationToTech.setFrontOfficeFcmTokenList(new HashSet<>());
                notificationClient.sendBulkPushNotification(sendBulkNotificationToTech);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
