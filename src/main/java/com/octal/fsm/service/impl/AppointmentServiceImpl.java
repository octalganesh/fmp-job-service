package com.octal.fsm.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netflix.discovery.converters.Auto;
import com.octal.fsm.dto.AppointmentDTO;
import com.octal.fsm.dto.JobTaskDTO;
import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.entities.*;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.*;
import com.octal.fsm.service.AppointmentService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private JobTagRepository jobTagRepository;

    @Autowired
    private JobTypeRepository jobTypeRepository;

    @Autowired
    private JobMappingTaskRepository jobMappingTaskRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private SpecificationFactory<Appointment>appointmentSpecificationFactory;

    @Override
    public String addAppointment(AppointmentDTO.Add add, String userName) throws CodeException {
        if(TextUtils.isEmpty(add.getJobId()))
            throw new CodeException("job id is required", ErrorCode.COMMON);
        if(TextUtils.isEmpty(add.getJobTypeId()))
            throw new CodeException("job type is required",ErrorCode.COMMON);
        Optional<JobType>jobType=jobTypeRepository.findByUuid(add.getJobTypeId());
        if(jobType.isEmpty())
            throw new CodeException("job type not found by id",ErrorCode.COMMON);
        if(add.getJobTags().isEmpty())
            throw new CodeException("job tags are required",ErrorCode.COMMON);
        List<JobTag> list=jobTagRepository.findByUuidIn(add.getJobTags());
        if(list.isEmpty())
            throw new CodeException("job tags not found with ids",ErrorCode.COMMON);
        if(TextUtils.isEmpty(add.getJobTaskId()))
            throw new CodeException("job task id  is required",ErrorCode.COMMON);
        Optional<JobMappingTask>jobTaskMappingTechnician=jobMappingTaskRepository.findByTaskShowId(add.getJobTaskId());
        if(jobTaskMappingTechnician.isEmpty())
            throw new CodeException("job task not found",ErrorCode.COMMON);
        if(add.getStartDateTime()==null)
            throw new CodeException("start date time cannot be null",ErrorCode.COMMON);
        if(add.getEndDateTime()==null)
            throw new CodeException("end date time cannot be null",ErrorCode.COMMON);
        if(TextUtils.isEmpty(add.getTechnicianId()))
            throw new CodeException("technician id is required",ErrorCode.COMMON);
        Appointment appointment;
        if (TextUtils.isEmpty(add.getId())) {
            appointment = new Appointment();
            appointment.setCreatedAt(LocalDateTime.now());
        } else {
            Optional<Appointment> announcementOptional = appointmentRepository.findByUuidAndDeletedFalse(add.getId());
            if (announcementOptional.isEmpty()) {
                throw new CodeException("Appointment not found", ErrorCode.COMMON);
            }
            appointment = announcementOptional.get();
            appointment.setUpdatedAt(LocalDateTime.now());
        }
        appointment.setActive(true);
        appointment.setDeleted(false);
        appointment.setAdditionalNotes(add.getAdditionalNotes());
        appointment.setJobId(add.getJobId());
        Gson gson = new Gson();
        appointment.setJobTags(gson.toJson(add.getJobTags()));
        appointment.setJobTypeId(add.getJobTypeId());
        appointment.setJobTaskId(add.getJobTaskId());
        appointment.setStartDateTime(add.getStartDateTime());
        appointment.setEndDateTime(add.getEndDateTime());
        appointment.setTechnicianId(add.getTechnicianId());
        appointment = appointmentRepository.save(appointment);
        return appointment.getUuid();
    }

    @Override
    public PageItem<AppointmentDTO.ListResponse> listAllAppointments(PageRequest.List listRequest, String userName) throws CodeException {
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        GenericSpecificationsBuilder<Appointment> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        prepareJobTypeSearchFilter(listRequest, builder);
        Page<Appointment> pagedResult = appointmentRepository.findAll(builder.build(), pageable);
        List<AppointmentDTO.ListResponse> responseList = new ArrayList<>();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        for (Appointment appointment : pagedResult.getContent()) {
            AppointmentDTO.ListResponse dto = new AppointmentDTO.ListResponse();
            dto.setAdditionalNotes(appointment.getAdditionalNotes());
            dto.setJobId(appointment.getJobId());
            dto.setJobTags(new Gson().fromJson(appointment.getJobTags(),listType));
            dto.setJobTypeId(appointment.getJobTypeId());
            dto.setJobTaskId(appointment.getJobTaskId());
            dto.setStartDateTime(appointment.getStartDateTime());
            dto.setEndDateTime(appointment.getEndDateTime());
            dto.setTechnicianId(appointment.getTechnicianId());
            responseList.add(dto);
        }

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(),
                listRequest.getPageSize());
    }

    private void prepareJobTypeSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<Appointment> builder) {
        builder.with(appointmentSpecificationFactory.isEqual("deleted", false));
        if (listRequest.getIsActive() != null) {
            builder.with(appointmentSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(appointmentSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(appointmentSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }

    }
}
