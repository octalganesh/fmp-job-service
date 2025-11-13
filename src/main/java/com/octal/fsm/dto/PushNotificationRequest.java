package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.octal.fsm.dto.enums.NotificationType;
import com.octal.fsm.dto.enums.NotificationUserGroup;
import com.octal.fsm.dto.enums.PushNotificationType;
import com.octal.fsm.entities.MultiUserDeviceDetails;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class PushNotificationRequest {

    private Long id;
    private String deviceType;
    private Set<MultiUserDeviceDetails> fcmDeviceToken;

    public PushNotificationRequest(Long id, String deviceType, Set<MultiUserDeviceDetails> fcmDeviceToken) {
        this.id = id;
        this.deviceType = deviceType;
        this.fcmDeviceToken = fcmDeviceToken;
    }

    @Data
    public static class Request {
        private PushNotificationType notificationType;
        private String notificationTypeId;
        private PushNotificationRequest systemUser;
        private String title;
        private String message;
        private String imageUrl;
        private String performedByUserId;
        private String fromDisplayName;
        private String contentMessage;
        private String replaceText;
    }

    @Data
    public static class RequestForNotification {
        private PushNotificationType notificationType;
        private String userUuid;
        private String imageUrl;
        private String replaceText;
    }

    @Data
    public static class SendBulkNotificationToUsers{
        public Set<MultiUserDeviceDetails> technicianFcmTokenList;
        public Set<MultiUserDeviceDetails> frontOfficeFcmTokenList;
        public PushNotificationType type;
        public String title;
        private String body;
        private String imageUrl;
        private String typeId;
        public String topic;
    }

    @Data
    public static class NotificationChannelRequest {
        @NotBlank(message = "Title is required")
        private String title;
        @NotBlank(message = "Message is required")
        private String message;
        private String typeId;
        private String description;
        private String contectUrl;
        private Set<String> customerId;
        private NotificationUserGroup userGroup;
        @NotNull(message = "Notification type is required")
        private NotificationType notificationType;

    }


}
