package com.octal.fsm.service.impl;


import com.octal.fsm.dto.AppointmentTypeDTO;
import com.octal.fsm.entities.AppointmentType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.AppointmentTypeRepository;
import com.octal.fsm.service.AppointmentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentTypeServiceImpl implements AppointmentTypeService {

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @Override
    public AppointmentTypeDTO.Detail getAppointmentByUuid(String id, Long tenantId, boolean isSuperAdmin) throws CodeException {
        AppointmentType appointmentType = appointmentTypeRepository.findByUuid(id).orElseThrow(() -> new CodeException(
                "Appointment type not found with id: " + id,
                ErrorCode.RECORD_NOT_FOUND
        ));
        AppointmentTypeDTO.Detail dto = new AppointmentTypeDTO.Detail();
        dto.setId(appointmentType.getUuid());
        dto.setName(appointmentType.getName());
        dto.setDescription(appointmentType.getDescription());
        dto.setIsActive(appointmentType.getActive());
        dto.setCreatedAt(appointmentType.getCreatedAt().toString());
        dto.setUpdatedAt(appointmentType.getUpdatedAt().toString());
        return dto;
    }

    @Override
    public List<AppointmentTypeDTO.Detail> getAllAppointmentType(Long tenantId, Boolean isSuperAdmin) throws CodeException {
        List<AppointmentType>appointmentTypes= appointmentTypeRepository.findAll();
        List<AppointmentTypeDTO.Detail> responseList = new ArrayList<>();
        for(AppointmentType type:appointmentTypes) {
            AppointmentTypeDTO.Detail roleDTO = new AppointmentTypeDTO.Detail();
            roleDTO.setId(type.getUuid());
            roleDTO.setName(type.getName());
            roleDTO.setCreatedAt(type.getCreatedAt().toString());
            roleDTO.setUpdatedAt(type.getUpdatedAt().toString());
            roleDTO.setDescription(type.getDescription());
            roleDTO.setIsActive(type.getActive());
            responseList.add(roleDTO);
        }
        return responseList;
    }
}
