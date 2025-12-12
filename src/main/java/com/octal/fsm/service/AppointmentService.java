package com.octal.fsm.service;

import com.octal.fsm.dto.AppointmentDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;
import org.springframework.stereotype.Component;

@Component
public interface AppointmentService {

    String addAppointment(AppointmentDTO.Add add, String userName) throws CodeException;

    PageItem<AppointmentDTO.ListResponse> listAllAppointments(PageRequest.List list, Long tenantId, Boolean isSuperAdmin, String userName) throws CodeException;

    PageItem<AppointmentDTO.ListResponse> listAllAppointmentsWithTechnicianId(PageRequest.List list, Long tenantId, Boolean isSuperAdmin, String userName) throws CodeException;

    String updateAppointmentStatus(String id) throws CodeException;
}
