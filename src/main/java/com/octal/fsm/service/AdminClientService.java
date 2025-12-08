package com.octal.fsm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.CustomerDTO;
import com.octal.fsm.dto.FrontOfficeStaffDTO;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminClientService {

    @Autowired
    private AdminClient adminClient;
    @Autowired
    private ObjectMapper objectMapper;

    public CustomerDTO.GetDetails getCustomerById(String id) {
        try {
            if (TextUtils.isEmpty(id)) {
                return null;
            }
            ResponseEntity<ApiResponse> responseEntity = adminClient.getCustomerById(id);
            ApiResponse customerResponse = responseEntity.getBody();
            if (customerResponse == null || customerResponse.getStatus() == null
                    || !customerResponse.getStatus().equalsIgnoreCase("200") || customerResponse.getData() == null) {
                return null;
            }
            Gson gson = new Gson();
            return gson.fromJson(gson.toJson(customerResponse.getData()), CustomerDTO.GetDetails.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public FrontOfficeStaffDTO.list getFrontOfficeById(String id, Long tenantId) {
        try {
            if (TextUtils.isEmpty(id)) {
                return null;
            }
            ResponseEntity<ApiResponse> responseEntity = adminClient.getFrontOfficeById(id, tenantId);
            ApiResponse frontOfficeResponse = responseEntity.getBody();
            if (frontOfficeResponse == null || frontOfficeResponse.getStatus() == null
                    || !frontOfficeResponse.getStatus().equalsIgnoreCase("200") || frontOfficeResponse.getData() == null) {
                return null;
            }
            Gson gson = new Gson();
            return gson.fromJson(gson.toJson(frontOfficeResponse.getData()), FrontOfficeStaffDTO.list.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<CustomerDTO.GetDetails> getCustomerList(List<String> ids, Long tenantId, boolean isSuperAdmin) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> uniqueIds = ids.stream().distinct().collect(Collectors.toList());

        ResponseEntity<ApiResponse> responseEntity = adminClient.getCustomerByIds(uniqueIds, tenantId, isSuperAdmin);

        ApiResponse customerResponse = responseEntity.getBody();

        if (customerResponse == null
                || customerResponse.getStatus() == null
                || !customerResponse.getStatus().equalsIgnoreCase("200")
                || customerResponse.getData() == null) {
            return Collections.emptyList();
        }

        List<CustomerDTO.GetDetails> techDetails = objectMapper.convertValue(
                customerResponse.getData(),
                new TypeReference<List<CustomerDTO.GetDetails>>() {
                }
        );
        return techDetails;
    }
}
