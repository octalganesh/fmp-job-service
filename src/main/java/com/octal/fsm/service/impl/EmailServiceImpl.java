package com.octal.fsm.service.impl;

import com.google.gson.Gson;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.EmailDTO;
import com.octal.fsm.dto.EmailTemplateDto;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.service.EmailService;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.logging.Logger;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AdminClient adminClient;

    public void sendMail(EmailDTO mail, String loggedInuser) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(mail.getMailTo());
        EmailTemplateDto.EmailTemplateRequest request = new EmailTemplateDto.EmailTemplateRequest(mail.getTemplateName(), mail.getProps());

        // Get subject and body from DB
        ResponseEntity<ApiResponse> subjectAndBody = adminClient.getTemplateContent(request, loggedInuser);

        if (subjectAndBody != null && subjectAndBody.getBody() != null && subjectAndBody.getBody().getData() != null) {
            Gson gson = new Gson();
            String stringResponse = gson.toJson(subjectAndBody.getBody().getData());
            EmailTemplateDto.EmailTemplateResponse response = gson.fromJson(stringResponse, EmailTemplateDto.EmailTemplateResponse.class);
            if (response != null) {
                helper.setSubject(response.getSubject());
                helper.setText(response.getBody(), true); // true = HTML email

                mailSender.send(message);
            }

        }

    }
}