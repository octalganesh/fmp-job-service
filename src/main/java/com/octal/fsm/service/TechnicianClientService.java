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

    public List<TechnicianDTO.GetDetails> getTechniciansList(List<String> ids, Long tenantId) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> uniqueIds = ids.stream().distinct().collect(Collectors.toList());

        ResponseEntity<ApiResponse> responseEntity = technicianClient.getTechByIds(uniqueIds, tenantId);

        ApiResponse technicianResponse = responseEntity.getBody();

        if (technicianResponse == null
                || technicianResponse.getStatus() == null
                || !technicianResponse.getStatus().equalsIgnoreCase("200")
                || technicianResponse.getData() == null) {
            return Collections.emptyList();
        }

        List<TechnicianDTO.GetDetails> techDetails = objectMapper.convertValue(
                technicianResponse.getData(),
                new TypeReference<List<TechnicianDTO.GetDetails>>() {
                }
        );
        return techDetails;
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
            Gson gson = new Gson();
            return gson.fromJson(gson.toJson(technicianResponse.getData()), TechnicianDTO.GetDetails.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }
}
