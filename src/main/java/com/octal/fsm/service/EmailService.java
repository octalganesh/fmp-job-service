package com.octal.fsm.service;

import com.octal.fsm.dto.EmailDTO;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;

@Component
public interface EmailService /*extends DefaultEmailService<User>*/ {

    void sendMail(EmailDTO mail, String loggedInuser) throws MessagingException;


}