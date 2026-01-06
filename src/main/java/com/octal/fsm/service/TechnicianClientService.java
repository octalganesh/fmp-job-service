package com.octal.fsm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.octal.fsm.clients.TechnicianClient;
import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.TechnicianDTO;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianClientService {

    @Autowired
    private TechnicianClient technicianClient;
    @Autowired
    private ObjectMapper objectMapper;
    private static final Gson GSON = new Gson();

    public List<TechnicianDTO.GetDetails> getTechniciansList(List<String> ids, Long tenantId) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> uniqueIds = ids.stream().distinct().collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return Collections.emptyList();
        }

        ResponseEntity<ApiResponse> responseEntity = technicianClient.getTechByIds(uniqueIds, tenantId);

        ApiResponse technicianResponse = responseEntity.getBody();

        if (technicianResponse == null || technicianResponse.getStatus() == null || !technicianResponse.getStatus().equalsIgnoreCase("200")
                || technicianResponse.getData() == null) {
            return Collections.emptyList();
        }
        return objectMapper.convertValue(technicianResponse.getData(), new TypeReference<List<TechnicianDTO.GetDetails>>() {
                }
        );
    }

    public TechnicianDTO.GetDetails getTechnicianById(String id, String userName) {
        try {
            if (TextUtils.isEmpty(id)) {
                return null;
            }
            ResponseEntity<ApiResponse> responseEntity = technicianClient.getTechnicianById(id, userName);
            ApiResponse technicianResponse = responseEntity.getBody();
            if (technicianResponse == null || technicianResponse.getStatus() == null
                    || !technicianResponse.getStatus().equalsIgnoreCase("200") || technicianResponse.getData() == null) {
                return null;
            }
            Object data = technicianResponse.getData();
            return GSON.fromJson(GSON.toJson(data), TechnicianDTO.GetDetails.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }
}
