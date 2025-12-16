package com.octal.fsm.service.impl;

import com.google.gson.Gson;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.EmailDTO;
import com.octal.fsm.dto.EmailRequestDTO;
import com.octal.fsm.dto.EmailTemplateDto;
import com.octal.fsm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private NotificationClient notificationClient;

    public void sendMail(EmailDTO mail, String loggedInuser, Long tenantId, boolean isSuperAdmin){
        try{
            EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
            emailRequestDTO.setToMail(mail.getMailTo());

            EmailTemplateDto.EmailTemplateRequest request = new EmailTemplateDto.EmailTemplateRequest(mail.getTemplateName(), mail.getProps());
            // Get subject and body from DB
            ResponseEntity<ApiResponse> subjectAndBody = adminClient.getTemplateContent(request, loggedInuser, tenantId, isSuperAdmin);

            if (subjectAndBody != null && subjectAndBody.getBody() != null && subjectAndBody.getBody().getData() != null) {
                Gson gson = new Gson();
                String stringResponse = gson.toJson(subjectAndBody.getBody().getData());
                EmailTemplateDto.EmailTemplateResponse response = gson.fromJson(stringResponse, EmailTemplateDto.EmailTemplateResponse.class);
                if (response != null) {
                    emailRequestDTO.setSubject(response.getSubject());
                    emailRequestDTO.setBody(response.getBody());
                    if (mail.getAttachments() != null) {
                        List<String> attachments = mail.getAttachments()
                                .stream().map(String::valueOf)  // converts Object → String safely
                                .collect(Collectors.toList());
                        emailRequestDTO.setAttachments(attachments);
                    }
                    notificationClient.sendEmailToUser(emailRequestDTO);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}