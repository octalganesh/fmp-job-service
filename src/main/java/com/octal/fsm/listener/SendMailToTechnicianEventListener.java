package com.octal.fsm.listener;

import com.octal.fsm.dto.EmailDTO;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.TechnicianDTO;
import com.octal.fsm.listener.events.SendMailToTechnicianEvent;
import com.octal.fsm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class SendMailToTechnicianEventListener implements ApplicationListener<SendMailToTechnicianEvent> {

    @Autowired
    private EmailService emailService;

    @Override
    @Async("sendMailToTechnicianEvent")
    public void onApplicationEvent(SendMailToTechnicianEvent event) {
        TechnicianDTO.TechnicianData technicianDTO = event.getTechnicianDTO();
        JobDTO.Detail jobDetails = event.getJobDetails(); // Assuming your event has job details

        sendJobEmailToTechnician(technicianDTO, jobDetails, event.getLoggedInuser());
    }

    private void sendJobEmailToTechnician(TechnicianDTO.TechnicianData technician, JobDTO.Detail jobDetails, String loggedInuser) {
        try {
            // ✅ Prepare dynamic placeholders
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("technicianName", technician.getName());
            placeholders.put("jobId", jobDetails.getJobId());
            placeholders.put("jobType", jobDetails.getJobType());
            placeholders.put("customerName", jobDetails.getCustomerDetails().getCustomerName());
            placeholders.put("serviceLocation", jobDetails.getServiceLocation());
            placeholders.put("startDate", jobDetails.getJobStartDate());
            placeholders.put("endDate", jobDetails.getJobEndDate());
            placeholders.put("logoUrl", "https://yourcdn.com/logo.png");

            // ✅ Prepare email data
            EmailDTO mail = new EmailDTO();
            mail.setMailTo(technician.getEmail());
            mail.setTemplateName("TECHNICIAN_JOB_ASSIGNED");
            mail.setProps(placeholders);

            // ✅ Send email using your unified sendMail method
            emailService.sendMail(mail, loggedInuser);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}