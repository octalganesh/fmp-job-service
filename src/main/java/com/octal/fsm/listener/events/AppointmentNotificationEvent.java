package com.octal.fsm.listener.events;

import com.octal.fsm.entities.Appointment;
import org.springframework.context.ApplicationEvent;

public class AppointmentNotificationEvent extends ApplicationEvent {

    private final Appointment appointment;
    private final String loggedInser;

    public AppointmentNotificationEvent(Object source, Appointment appointment, String loggedInser) {
        super(source);
        this.appointment = appointment;
        this.loggedInser = loggedInser;
    }
    public Appointment getAppointment() {
        return appointment;
    }

    public String getLoggedInuser() {
        return loggedInser;
    }
}
