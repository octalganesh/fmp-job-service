package com.octal.fsm.service;

import com.octal.fsm.dto.AppointmentTypeDTO;
import com.octal.fsm.entities.AppointmentType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
public interface AppointmentTypeService {

    AppointmentTypeDTO.Detail getAppointmentByUuid(String id, Long tenantId, boolean isSuperAdmin) throws CodeException;

    List<AppointmentTypeDTO.Detail> getAllAppointmentType(Long tenantId, Boolean isSuperAdmin) throws CodeException;

}
