package com.octal.fsm.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.octal.fsm.client.QuickBooksClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.entities.QuickBooksToken;
import com.octal.fsm.exceptions.*;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
public class QuickBooksCustomerService {

    @Autowired
    private QuickBooksClient quickBooksClient;

    @Autowired
    private QuickBooksTokenStore tokenStore;

    @Value("${quickbooks.api.minorversion}")
    private String minorVersion;
    @Value("${quickbooks.company-id}")
    private String realmId;


    public JsonNode createCustomer(CustomerRequest customerRequest) throws Exception {
        QuickBooksToken tokenInfo = tokenStore.getToken(realmId);

        if (tokenInfo == null) {

            throw new Exception("No token found. Please connect to QuickBooks first.");
        }

        String bearerToken = "Bearer " + tokenInfo.getAccessToken();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(customerRequest);


        try {
            String jsonResponse = quickBooksClient.createCustomer(bearerToken, minorVersion, json, realmId);
            // Convert XML to Java object

            ObjectMapper objectMapper1 = new ObjectMapper();
            JsonNode node = objectMapper1.readTree(jsonResponse);
            return node;
        } catch (FeignException.BadRequest e) {
            String responseBody = e.contentUTF8();
            ObjectMapper mapper = new ObjectMapper();
            QuickBooksErrorInfo info = null;

            try {
                JsonNode root = mapper.readTree(responseBody);
                JsonNode faultNode = root.path("Fault");
                JsonNode errorsArray = faultNode.path("Error");

                if (errorsArray.isArray() && errorsArray.size() > 0) {
                    JsonNode first = errorsArray.get(0);
                    String message = first.path("Message").asText(null);
                    String detail = first.path("Detail").asText(null);
                    String code = first.path("code").asText(null);
                    info = new QuickBooksErrorInfo(code, message, detail, responseBody);
                }
            } catch (Exception ex) {
                throw new CodeException("Unable to parse QuickBooks error", ErrorCode.COMMON);
             }

            if ("6240".equals(info.getCode())) {
                QuickBooksErrorInfo errorInfo = new QuickBooksErrorInfo();
                errorInfo.setMessage(info.getMessage());
                errorInfo.setCode(info.getCode());
                errorInfo.setDetail(info.getDetail());
                errorInfo.setRaw(info.getRaw());
                throw new CodeException(errorInfo.getMessage(), ErrorCode.COMMON);
             } else {
                throw new CodeException(info.getMessage(), ErrorCode.COMMON);
             }

        } catch (Exception e) {
            throw new CodeException("Customer creation failed: " +e.getMessage(), ErrorCode.COMMON);
         }
    }

    public CreateInvoiceDTO createInvoice(InvoiceRequest invoiceRequest) throws Exception {
        QuickBooksToken tokenInfo = tokenStore.getToken(realmId);
        if (tokenInfo == null) {
            throw new Exception("No token found. Please connect to QuickBooks first.");
        }
        String bearerToken = "Bearer " + tokenInfo.getAccessToken();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(invoiceRequest);

        try {
            String jsonResponse = quickBooksClient.createInvoice(bearerToken, minorVersion, json, realmId);
            Gson gson = new Gson();
            CreateInvoiceDTO createInvoiceDTO = gson.fromJson(jsonResponse, CreateInvoiceDTO.class);
            return createInvoiceDTO;
        } catch (FeignException.BadRequest e) {
            String responseBody = e.contentUTF8();
            ObjectMapper mapper = new ObjectMapper();
            QuickBooksErrorInfo info = null;

            try {
                JsonNode root = mapper.readTree(responseBody);
                JsonNode errorsArray = root.path("Fault").path("Error");

                if (errorsArray.isArray() && errorsArray.size() > 0) {
                    JsonNode first = errorsArray.get(0);
                    String message = first.path("Message").asText(null);
                    String detail = first.path("Detail").asText(null);
                    String code = first.path("code").asText(null);
                    info = new QuickBooksErrorInfo(code, message, detail, responseBody);
                }
            } catch (Exception ex) {
                info = new QuickBooksErrorInfo(null, "Unable to parse QuickBooks error", null, responseBody);
            }

            throw new QuickBooksClientException(info);

        } catch (Exception e) {
            throw new RuntimeException("Invoice creation failed: " + e.getMessage(), e);
        }
    }


    public JsonNode sendInvoice(String invoiceId, String sendTo) throws Exception {
        QuickBooksToken tokenInfo = tokenStore.getToken(realmId);

        if (tokenInfo == null) {
            throw new Exception("No token found. Please connect to QuickBooks first.");
        }

        String bearerToken = "Bearer " + tokenInfo.getAccessToken();

        try {

            // Call QuickBooks API to send the invoice
            String jsonResponse = quickBooksClient.sendInvoice(bearerToken, realmId, invoiceId, sendTo, minorVersion);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonResponse);

            return node;

        } catch (FeignException.BadRequest e) {
            String responseBody = e.contentUTF8();
            ObjectMapper mapper = new ObjectMapper();
            QuickBooksErrorInfo info = null;

            try {
                JsonNode root = mapper.readTree(responseBody);
                JsonNode errorsArray = root.path("Fault").path("Error");

                if (errorsArray.isArray() && errorsArray.size() > 0) {
                    JsonNode first = errorsArray.get(0);
                    String message = first.path("Message").asText(null);
                    String detail = first.path("Detail").asText(null);
                    String code = first.path("code").asText(null);
                    info = new QuickBooksErrorInfo(code, message, detail, responseBody);
                }
            } catch (Exception ex) {
                info = new QuickBooksErrorInfo(null, "Unable to parse QuickBooks error", null, responseBody);
            }

            throw new QuickBooksClientException(info);

        } catch (Exception e) {
            throw new RuntimeException("Invoice sending failed: " + e.getMessage(), e);
        }
    }


    public QuickBookApiResponse createItem(QuickBooksItemDTO itemDTO) throws Exception {
        QuickBooksToken tokenInfo = tokenStore.getToken(realmId);
        if (tokenInfo == null) throw new Exception("No token found. Connect to QuickBooks first.");

        String bearerToken = "Bearer " + tokenInfo.getAccessToken();

        try {
            // Convert DTO to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(itemDTO);

            // Call QuickBooks API
            String jsonResponse = quickBooksClient.createItem(bearerToken, realmId, payload, minorVersion);
            JsonNode node = objectMapper.readTree(jsonResponse);

            String itemId = node.path("Item").path("Id").asText();

            return new QuickBookApiResponse<>(HttpStatus.OK.value(), true, "Item created successfully in QuickBooks. Item ID: " + itemId, node);

        } catch (FeignException.BadRequest e) {
            String responseBody = e.contentUTF8();
            ObjectMapper mapper = new ObjectMapper();
            QuickBooksErrorInfo info = null;

            try {
                JsonNode root = mapper.readTree(responseBody);
                JsonNode errorsArray = root.path("Fault").path("Error");
                if (errorsArray.isArray() && errorsArray.size() > 0) {
                    JsonNode first = errorsArray.get(0);
                    String message = first.path("Message").asText(null);
                    String detail = first.path("Detail").asText(null);
                    String code = first.path("code").asText(null);
                    info = new QuickBooksErrorInfo(code, message, detail, responseBody);
                }
            } catch (Exception ex) {
                info = new QuickBooksErrorInfo(null, "Unable to parse QuickBooks error", null, responseBody);
            }

            throw new QuickBooksClientException(info);

        } catch (Exception e) {
            throw new RuntimeException("Item creation failed: " + e.getMessage(), e);
        }
    }

    public JsonNode getAllItems(int startPosition, int maxResults) throws Exception {
        // Get QuickBooks access token
        QuickBooksToken tokenInfo = tokenStore.getToken(realmId);
        if (tokenInfo == null) {
            throw new Exception("No token found. Connect to QuickBooks first.");
        }
        String bearerToken = "Bearer " + tokenInfo.getAccessToken();

        try {
            // Build the query
            String query = String.format("select * from item startposition %d maxresults %d", startPosition, maxResults);

            // Call QuickBooks API
            String jsonResponse = quickBooksClient.queryItems(bearerToken, realmId, query, minorVersion);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonResponse);

            return node;

        } catch (FeignException.BadRequest e) {
            String responseBody = e.contentUTF8();
            QuickBooksErrorInfo info = parseQuickBooksError(responseBody);
            throw new QuickBooksClientException(info);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch items: " + e.getMessage(), e);
        }
    }

    // Reuse the error parser from createItem
    private QuickBooksErrorInfo parseQuickBooksError(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode errorsArray = root.path("Fault").path("Error");

            if (errorsArray.isArray() && errorsArray.size() > 0) {
                JsonNode first = errorsArray.get(0);
                String message = first.path("Message").asText(null);
                String detail = first.path("Detail").asText(null);
                String code = first.path("code").asText(null);
                return new QuickBooksErrorInfo(code, message, detail, responseBody);
            }
        } catch (Exception ex) {
            return new QuickBooksErrorInfo(null, "Unable to parse QuickBooks error", null, responseBody);
        }
        return new QuickBooksErrorInfo(null, "Unknown QuickBooks error", null, responseBody);
    }

    public String addNewCustomer(@Valid QuickBookDTO.CreateCustomer quickBookDTO) throws Exception {
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setDisplayName(quickBookDTO.getName() + "-" + quickBookDTO.getEmail());
        CustomerRequest.PrimaryEmailAddr primaryEmailAddr = new CustomerRequest.PrimaryEmailAddr();
        primaryEmailAddr.setAddress(quickBookDTO.getEmail());
        customerRequest.setPrimaryEmailAddr(primaryEmailAddr);
        CustomerRequest.PrimaryPhone primaryPhone = new CustomerRequest.PrimaryPhone();
        primaryPhone.setFreeFormNumber(quickBookDTO.getMobileNumber());
        customerRequest.setPrimaryPhone(primaryPhone);
        CustomerRequest.BillAddr billAddr = new CustomerRequest.BillAddr();
        billAddr.setCountry(quickBookDTO.getPrimaryLocation());
        billAddr.setLine1(quickBookDTO.getAddress());
        customerRequest.setBillAddr(billAddr);
        JsonNode node = createCustomer(customerRequest);

        ObjectMapper mapper = new ObjectMapper();
        QuickBooksCustomerResponseDTO.DataObject dataObject = mapper.treeToValue(node, QuickBooksCustomerResponseDTO.DataObject.class);


        String customerId = dataObject.getCustomer().getId();
        return customerId;
    }
}
