package com.octal.fsm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.NotificationContentDTO;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class NotificationClientService {

    @Autowired
    private NotificationClient notificationClient;
    @Autowired
    private ObjectMapper objectMapper;

    public NotificationContentDTO.Request getNotificationContent(String slug) {
        try {
            if (TextUtils.isEmpty(slug)) {
                return null;
            }
            ResponseEntity<ApiResponse> responseEntity = notificationClient.getNotificationContent(slug);
            ApiResponse contentResponse = responseEntity.getBody();
            if (contentResponse == null || contentResponse.getStatus() == null
                    || !contentResponse.getStatus().equalsIgnoreCase("200") || contentResponse.getData() == null) {
                return null;
            }
            Gson gson = new Gson();
            return gson.fromJson(gson.toJson(contentResponse.getData()), NotificationContentDTO.Request.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
