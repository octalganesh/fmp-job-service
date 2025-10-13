package com.octal.fsm.listener.events;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.TechnicianDTO;
import org.springframework.context.ApplicationEvent;

public class SendMailToTechnicianEvent extends ApplicationEvent {
    private final TechnicianDTO.TechnicianData technicianDTO;
    private final JobDTO.Detail jobDetails;
    private final String loggedInuser;

    public SendMailToTechnicianEvent(TechnicianDTO.TechnicianData technicianDTO, JobDTO.Detail jobDetails, String loggedInuser) {
        super(technicianDTO);
        this.technicianDTO = technicianDTO;
        this.jobDetails = jobDetails;
        this.loggedInuser = loggedInuser;
    }

    public TechnicianDTO.TechnicianData getTechnicianDTO() {
        return technicianDTO;
    }

    public JobDTO.Detail getJobDetails() {
        return jobDetails;
    }

    public String getLoggedInuser() {
        return loggedInuser;
    }
}
