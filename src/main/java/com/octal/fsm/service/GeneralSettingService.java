package com.octal.fsm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.GeneralSettingDTO;
import com.octal.fsm.dto.enums.SettingKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
public class GeneralSettingService {

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private ObjectMapper objectMapper;


    public int getPageSize(Long tenantId) {
        return getInt(tenantId, SettingKey.RECORDS_PER_PAGE);
    }

    public boolean isMaintenanceEnabled(Long tenantId) {
        return getBoolean(tenantId, SettingKey.MAINTENANCE_MODE);
    }

    public boolean isNotificationEnabled(Long tenantId) {
        return getBoolean(tenantId, SettingKey.NOTIFICATION_ENABLED);
    }

    public String getDateFormat(Long tenantId) {
        return getString(tenantId, SettingKey.DATE_FORMAT);
    }

    public String getDefaultCurrency(Long tenantId) {
        return getString(tenantId, SettingKey.DEFAULT_CURRENCY);
    }

    public String getTimeFormat(Long tenantId) {
        return getString(tenantId, SettingKey.TIME_FORMAT);
    }


    public int getInt(Long tenantId, SettingKey key) {
        try {
            return Integer.parseInt(Objects.requireNonNull(getSettingValue(tenantId, key)));
        } catch (Exception e) {
            return 5;
        }
    }

    public String getString(Long tenantId, SettingKey key) {
        return getSettingValue(tenantId, key);
    }


    public boolean getBoolean(Long tenantId, SettingKey key) {
        return Boolean.parseBoolean(getSettingValue(tenantId, key)
        );
    }


    public Map<String, GeneralSettingDTO.Details> getAllMap(Long tenantId) {
        try {
            ResponseEntity<ApiResponse> response = adminClient.getGeneralSettingMap(tenantId);
            if (response == null || response.getBody() == null || response.getBody().getData() == null) {
                return Collections.emptyMap();
            }
            Object data = response.getBody().getData();
            return objectMapper.convertValue(data, new TypeReference<Map<String, GeneralSettingDTO.Details>>() {
            });
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String getSettingValue(Long tenantId, SettingKey key) {
        try {
            ResponseEntity<ApiResponse> response = adminClient.getGeneralSettingBYKey(key.name(), tenantId);
            if (response != null && response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData().toString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }

    }


}
