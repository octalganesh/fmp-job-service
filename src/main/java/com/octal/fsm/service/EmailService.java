package com.octal.fsm.service;

import com.octal.fsm.dto.EmailDTO;
import org.springframework.stereotype.Component;


@Component
public interface EmailService /*extends DefaultEmailService<User>*/ {

    void sendMail(EmailDTO mail, String loggedInuser, Long tenantId, boolean isSuperAdmin) ;


}