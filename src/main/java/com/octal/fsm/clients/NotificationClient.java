package com.octal.fsm.clients;


import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.PushNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "NOTIFICATION-SERVICE")
@Service
public interface NotificationClient {

    @PostMapping(value = "/notification/send/push-notification")
    ResponseEntity<ApiResponse> sendPushNotification(PushNotificationRequest.Request pushNotificationRequest);

//    @PostMapping(value = "/notification/notification-list")
//    ResponseEntity<ApiResponse> getUserNotificationList(@RequestBody UserNotificationListDTO.ListRequest request);

    @GetMapping(value = "/notification/notification-count")
    ApiResponse getUserNotificationCount(@RequestParam("userId") String userId);

    @GetMapping(value = "/notification/notification-seen")
    ApiResponse setUserNotificationSeen(@RequestParam("userId") String userId);

    @PostMapping(value = "/notification/send-bulk-notification-users")
    ResponseEntity<ApiResponse> sendBulkPushNotification(PushNotificationRequest.SendBulkNotificationToUsers pushNotificationRequest);

    @GetMapping(value = "/notification-content/get-by-slug")
    ResponseEntity<ApiResponse> getNotificationContent(@RequestParam("slug") String slug);


}