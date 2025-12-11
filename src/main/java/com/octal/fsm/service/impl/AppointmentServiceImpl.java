package com.octal.fsm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.clients.TechnicianClient;
import com.octal.fsm.dto.*;
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
import java.util.*;
import java.util.stream.Collectors;

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
    private SpecificationFactory<Appointment> appointmentSpecificationFactory;

    @Autowired
    private TechnicianClient technicianClient;
    @Autowired
    private AdminClient adminClient;
    @Autowired
    private JobRepository jobRepository;

    @Override
    public String addAppointment(AppointmentDTO.Add add, String userName) throws CodeException {
        if (TextUtils.isEmpty(add.getJobId()))
            throw new CodeException("job id is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(add.getJobTypeId()))
            throw new CodeException("job type is required", ErrorCode.COMMON);
        Optional<JobType> jobType = jobTypeRepository.findByUuid(add.getJobTypeId());
        if (jobType.isEmpty())
            throw new CodeException("job type not found by id", ErrorCode.COMMON);
        if (add.getJobTags().isEmpty())
            throw new CodeException("job tags are required", ErrorCode.COMMON);
        List<JobTag> list = jobTagRepository.findByUuidIn(add.getJobTags());
        if (list.isEmpty())
            throw new CodeException("job tags not found with ids", ErrorCode.COMMON);
        if (TextUtils.isEmpty(add.getJobTaskId()))
            throw new CodeException("job task id  is required", ErrorCode.COMMON);
        Optional<JobMappingTask> jobTaskMappingTechnician = jobMappingTaskRepository.findByTaskShowId(add.getJobTaskId());
        if (jobTaskMappingTechnician.isEmpty())
            throw new CodeException("job task not found", ErrorCode.COMMON);
        if (add.getStartDateTime() == null)
            throw new CodeException("start date time cannot be null", ErrorCode.COMMON);
        if (add.getEndDateTime() == null)
            throw new CodeException("end date time cannot be null", ErrorCode.COMMON);
        if (TextUtils.isEmpty(add.getTechnicianId()))
            throw new CodeException("technician id is required", ErrorCode.COMMON);
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
        appointment.setStatus("Scheduled");
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
    public PageItem<AppointmentDTO.ListResponse> listAllAppointments(PageRequest.List listRequest, Long tenantId, Boolean isSuperAdmin, String userName) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
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
        Map<String, TechnicianDTO.GetDetails> techMap = null;
        Map<String, CustomerDTO.GetDetails> customerToNameMap = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<Job> byUuidIn = jobRepository.findByJobIdIn(new ArrayList<>(pagedResult.getContent().stream().map(Appointment::getJobId).collect(Collectors.toList())));
        Map<String, Job> jobMap = byUuidIn.stream()
                .collect(Collectors.toMap(Job::getJobId, j -> j));

        List<JobMappingTask> byJobTaskIdIn = jobMappingTaskRepository.findByTaskShowIdIn(pagedResult.getContent().stream().map(Appointment::getJobTaskId).filter(Objects::nonNull).distinct().collect(Collectors.toList()));

        Map<String, JobMappingTask> jobMappingTaskMap = byJobTaskIdIn.stream()
                .collect(Collectors.toMap(JobMappingTask::getTaskShowId, j -> j));

        if(!byUuidIn.isEmpty()) {
            ApiResponse customerResponse = adminClient.getCustomerByIds(new ArrayList<>(byUuidIn.stream().map(Job::getCustomerId).filter(Objects::nonNull).distinct().collect(Collectors.toList())), tenantId, false).getBody();
            if (customerResponse != null && "200".equalsIgnoreCase(customerResponse.getStatus()) && customerResponse.getData() != null) {
                List<CustomerDTO.GetDetails> customerDetails = objectMapper.convertValue(
                        customerResponse.getData(),
                        new TypeReference<List<CustomerDTO.GetDetails>>() {
                        }
                );
                customerToNameMap = customerDetails.stream()
                        .collect(Collectors.toMap(CustomerDTO.GetDetails::getId, t -> t));
            }
        }

        ApiResponse technicianResponse = technicianClient.getTechByIds(new ArrayList<>(pagedResult.getContent().stream().map(Appointment::getTechnicianId).filter(Objects::nonNull).distinct().collect(Collectors.toList())), tenantId).getBody();
        if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200") && technicianResponse.getData() != null) {
            List<TechnicianDTO.GetDetails> techDetails = objectMapper.convertValue(
                    technicianResponse.getData(),
                    new TypeReference<List<TechnicianDTO.GetDetails>>() {
                    }
            );

            techMap = techDetails.stream()
                    .collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));
        }
        List<AppointmentDTO.ListResponse> responseList = new ArrayList<>();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<JobType>jobTypeList=jobTypeRepository.findByUuidAndDeletedFalse(pagedResult.getContent().stream().map(Appointment::getJobTypeId).collect(Collectors.toList()));
        Map<String, String> jobTypeMap = jobTypeList.stream()
                .collect(Collectors.toMap(JobType::getUuid, JobType::getName));
        for (Appointment appointment : pagedResult.getContent()) {
            AppointmentDTO.ListResponse dto = new AppointmentDTO.ListResponse();
            if (techMap != null) {
                TechnicianDTO.GetDetails tech = techMap.get(appointment.getTechnicianId());
                if (tech != null) {
                    dto.setTechnicianName(tech.getName());
                }
            }
            dto.setId(appointment.getUuid());
            dto.setAdditionalNotes(appointment.getAdditionalNotes());
            dto.setJobId(appointment.getJobId());
            dto.setJobTags(new Gson().fromJson(appointment.getJobTags(), listType));
            dto.setJobTypeId(appointment.getJobTypeId());
            dto.setJobTypeName(jobTypeMap.get(appointment.getJobTypeId()));
            dto.setJobTaskId(appointment.getJobTaskId());
            dto.setStartDateTime(appointment.getStartDateTime());
            dto.setEndDateTime(appointment.getEndDateTime());
            dto.setTechnicianId(appointment.getTechnicianId());
            dto.setStatus(appointment.getStatus() != null ? appointment.getStatus() : "Scheduled");
            Job job = jobMap.get(appointment.getJobId());
            if (job != null && customerToNameMap != null) {
                CustomerDTO.GetDetails customer = customerToNameMap.get(job.getCustomerId());
                if (customer != null) {
                    dto.setCustomerName(customer.getName());
                }
            }
            JobMappingTask jobMappingTask = jobMappingTaskMap.get(appointment.getJobTaskId());
            if (jobMappingTask != null) {
                dto.setTaskName(jobMappingTask.getTaskName());
            }
            responseList.add(dto);
        }

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(),
                listRequest.getPageSize());
    }

    @Override
    public PageItem<AppointmentDTO.ListResponse> listAllAppointmentsWithTechnicianId(PageRequest.List listRequest, Long tenantId, Boolean isSuperAdmin, String userName) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        GenericSpecificationsBuilder<Appointment> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        prepareJobTypeSearchFilterForTechnicianId(listRequest, builder);
        Page<Appointment> pagedResult = appointmentRepository.findAll(builder.build(), pageable);
        Map<String, TechnicianDTO.GetDetails> techMap = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ApiResponse technicianResponse = technicianClient.getTechByIds(new ArrayList<>(pagedResult.getContent().stream().map(Appointment::getTechnicianId).collect(Collectors.toList())), tenantId).getBody();
        if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200") && technicianResponse.getData() != null) {
            List<TechnicianDTO.GetDetails> techDetails = objectMapper.convertValue(
                    technicianResponse.getData(),
                    new TypeReference<List<TechnicianDTO.GetDetails>>() {
                    }
            );

            techMap = techDetails.stream()
                    .collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));
        }
        List<AppointmentDTO.ListResponse> responseList = new ArrayList<>();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        for (Appointment appointment : pagedResult.getContent()) {
            AppointmentDTO.ListResponse dto = new AppointmentDTO.ListResponse();
            TechnicianDTO.GetDetails tech = Objects.requireNonNull(techMap).get(appointment.getTechnicianId());
            if (tech != null)
                dto.setTechnicianName(tech.getName());
            dto.setId(appointment.getUuid());
            dto.setAdditionalNotes(appointment.getAdditionalNotes());
            dto.setJobId(appointment.getJobId());
            dto.setJobTags(new Gson().fromJson(appointment.getJobTags(), listType));
            dto.setJobTypeId(appointment.getJobTypeId());
            dto.setJobTaskId(appointment.getJobTaskId());
            dto.setStartDateTime(appointment.getStartDateTime());
            dto.setEndDateTime(appointment.getEndDateTime());
            dto.setTechnicianId(appointment.getTechnicianId());
            dto.setStatus(appointment.getStatus() != null ? appointment.getStatus() : "Scheduled");
            responseList.add(dto);
        }
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(),
                listRequest.getPageSize());
    }

    @Override
    public String updateAppointmentStatus(String id) throws CodeException {
        if (TextUtils.isEmpty(id))
            throw new CodeException("appointment id is required", ErrorCode.COMMON);
        Optional<Appointment> appointmentOptional = appointmentRepository.findByUuidAndDeletedFalse(id);
        if (appointmentOptional.isEmpty())
            throw new CodeException("appointment not found", ErrorCode.COMMON);

        Appointment appointment = appointmentOptional.get();
        appointment.setStatus("Completed");
        appointmentRepository.save(appointment);
        return appointment.getUuid();
    }

    private void prepareJobTypeSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<Appointment> builder) {
        builder.with(appointmentSpecificationFactory.isEqual("deleted", false));
        if (listRequest.getIsActive() != null) {
            builder.with(appointmentSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (!TextUtils.isEmpty(listRequest.getJobId())) {
            builder.with(appointmentSpecificationFactory.like("jobId", listRequest.getJobId()).or(appointmentSpecificationFactory.isEqual("jobId", listRequest.getJobId())));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(appointmentSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(appointmentSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }

    }

    private void prepareJobTypeSearchFilterForTechnicianId(PageRequest.List listRequest, GenericSpecificationsBuilder<Appointment> builder) {
        builder.with(appointmentSpecificationFactory.isEqual("deleted", false));
        if (listRequest.getIsActive() != null) {
            builder.with(appointmentSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (listRequest.getTechnicianId() !=null && !listRequest.getTechnicianId().isEmpty()) {
            builder.with(appointmentSpecificationFactory.in("technicianId", listRequest.getTechnicianId()));
        }
        if (!TextUtils.isEmpty(listRequest.getJobId())) {
            builder.with(appointmentSpecificationFactory.like("jobId", listRequest.getJobId()).or(appointmentSpecificationFactory.isEqual("jobId", listRequest.getJobId())));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(appointmentSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(appointmentSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }

    }
}
