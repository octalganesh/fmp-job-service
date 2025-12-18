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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        if (tenantId == null) {
            return 5;
        }
        return getInt(tenantId, SettingKey.RECORDS_PER_PAGE);
    }

    public boolean isMaintenanceEnabled(Long tenantId) {
        if (tenantId == null) {
            return false;
        }
        return getBoolean(tenantId, SettingKey.MAINTENANCE_MODE);
    }

    public boolean isNotificationEnabled(Long tenantId) {
        if (tenantId == null) {
            return true;
        }
        return getBoolean(tenantId, SettingKey.NOTIFICATION_ENABLED);
    }

    public String getDateFormat(Long tenantId) {
        if (tenantId == null) {
            return "dd-mm-yyyy";
        }
        return getString(tenantId, SettingKey.DATE_FORMAT);
    }

    public String getDefaultCurrency(Long tenantId) {
        if (tenantId == null) {
            return "INR";
        }
        return getString(tenantId, SettingKey.DEFAULT_CURRENCY);
    }

    public String getTimeFormat(Long tenantId) {
        if (tenantId == null) {
            return "12";
        }
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

    public String formatDate(LocalDateTime dateTime, Long tenantId) {
        if (dateTime == null || tenantId == null) {
            return null;
        }
        String format = getDateFormat(tenantId);
        if (format == null || format.isBlank()) {
            format = "dd/MM/yyyy";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(format.toLowerCase())
        );
    }

    public DateTimeFormatter buildTenantDateTimeFormatter(Long tenantId) {
        String rawDateFormat = getDateFormat(tenantId);
        if (rawDateFormat == null || rawDateFormat.isBlank()) {
            rawDateFormat = "dd/MM/yyyy";
        }

        String dateFormat = normalizeDateFormat(rawDateFormat);

        String timeFormat = getTimeFormat(tenantId);

        String timePattern =
                "12".equals(timeFormat) ? "hh:mm a" : "HH:mm";

        return DateTimeFormatter.ofPattern(
                dateFormat + " " + timePattern
        );
    }

    public String formatDateTime(LocalDateTime dateTime, Long tenantId) {
        if (dateTime == null || tenantId == null) {
            return null;
        }
        String rawDateFormat = getDateFormat(tenantId);
        if (rawDateFormat == null || rawDateFormat.isBlank()) {
            rawDateFormat = "dd/MM/yyyy";
        }
        String dateFormat = normalizeDateFormat(rawDateFormat);
        String timeFormatSetting = getTimeFormat(tenantId);
        String timePattern = "12".equals(timeFormatSetting) ? "hh:mm a" : "HH:mm";

        String finalPattern = dateFormat + " " + timePattern;
        return dateTime.format(
                DateTimeFormatter.ofPattern(finalPattern)
        );
    }

    private String normalizeDateFormat(String format) {
        if (format == null) {
            return "dd/MM/yyyy";
        }
        return format
                .replace("YYYY", "yyyy")
                .replace("YY", "yy")
                .replace("DD", "dd")
                .replace("D", "d");
    }


}
