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
import com.octal.fsm.service.AdminClientService;
import com.octal.fsm.service.AppointmentService;
import com.octal.fsm.service.GeneralSettingService;
import com.octal.fsm.service.TechnicianClientService;
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
import java.time.format.DateTimeFormatter;
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
    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @Autowired
    private TechnicianClientService technicianClientService;

    @Autowired
    private AdminClientService adminClientService;

    @Autowired
    private GeneralSettingService generalSettingService;

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
            appointment.setStatus("Scheduled");
        } else {
            Optional<Appointment> announcementOptional = appointmentRepository.findByUuidAndDeletedFalse(add.getId());
            if (announcementOptional.isEmpty()) {
                throw new CodeException("Appointment not found", ErrorCode.COMMON);
            }
            appointment = announcementOptional.get();
            appointment.setUpdatedAt(LocalDateTime.now());
        }
        if (!TextUtils.isEmpty(add.getAppointmentTypeId())) {
            Optional<AppointmentType> byUuid = appointmentTypeRepository.findByUuid(add.getAppointmentTypeId());
            if (byUuid.isPresent()) {
                appointment.setAppointmentType(byUuid.get());
            }
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
        //listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
        List<String> jobIds = new ArrayList<>();
        if (listRequest.getFrontOfficeId() != null) {
            jobIds = jobRepository.findByFrontOfficeIdAndDeletedFalse(listRequest.getFrontOfficeId())
                    .stream().map(Job::getJobId).filter(Objects::nonNull)
                    .distinct().collect(Collectors.toList());

            if (jobIds.isEmpty()) {
                return new PageItem<>(0, 0, Collections.emptyList(), listRequest.getPageNumber(), listRequest.getPageSize());
            }
            if (!TextUtils.isEmpty(listRequest.getJobId())) {
                if (!jobIds.contains(listRequest.getJobId())) {
                    return new PageItem<>(0, 0, Collections.emptyList(), listRequest.getPageNumber(), listRequest.getPageSize());
                }
            }
        }
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        prepareJobTypeSearchFilter(listRequest, builder, jobIds);
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

        if (!byUuidIn.isEmpty()) {
            List<CustomerDTO.GetDetails> customerList = adminClientService.getCustomerList(new ArrayList<>(byUuidIn.stream().map(Job::getCustomerId).filter(Objects::nonNull).distinct().collect(Collectors.toList())), tenantId, false);
            if (customerList != null) {
                customerToNameMap = customerList.stream()
                        .collect(Collectors.toMap(CustomerDTO.GetDetails::getId, t -> t));
            }
        }
        List<TechnicianDTO.GetDetails> techDetails = technicianClientService.getTechniciansList(new ArrayList<>(pagedResult.getContent().stream().map(Appointment::getTechnicianId).collect(Collectors.toList())), tenantId);
        if (techDetails != null) {
            techMap = techDetails.stream()
                    .collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));
        }
        List<AppointmentDTO.ListResponse> responseList = new ArrayList<>();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<JobType> jobTypeList = jobTypeRepository.findByUuidAndDeletedFalse(pagedResult.getContent().stream().map(Appointment::getJobTypeId).collect(Collectors.toList()));
        Map<String, String> jobTypeMap = jobTypeList.stream()
                .collect(Collectors.toMap(JobType::getUuid, JobType::getName));
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
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
            dto.setStartDateTime(appointment.getStartDateTime() != null ? appointment.getStartDateTime().format(dateTimeFormatter) : null);
            dto.setEndDateTime(appointment.getEndDateTime() != null ? appointment.getEndDateTime().format(dateTimeFormatter) : null);
            dto.setTechnicianId(appointment.getTechnicianId());
            dto.setStatus(appointment.getStatus() != null ? appointment.getStatus() : "Scheduled");
            dto.setAppointmentTypeId(appointment.getAppointmentType() != null ? appointment.getAppointmentType().getUuid() : null);
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
            dto.setAppointmentTypeId(appointment.getAppointmentType() != null ? appointment.getAppointmentType().getUuid() : null);
            dto.setAppointmentTypeName(appointment.getAppointmentType() != null ? appointment.getAppointmentType().getName() : null);
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
        //listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        prepareJobTypeSearchFilterForTechnicianId(listRequest, builder);
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

        if (!byUuidIn.isEmpty()) {
            List<CustomerDTO.GetDetails> customerList = adminClientService.getCustomerList(new ArrayList<>(byUuidIn.stream().map(Job::getCustomerId).filter(Objects::nonNull).distinct().collect(Collectors.toList())), tenantId, false);
            if (customerList != null) {
                customerToNameMap = customerList.stream()
                        .collect(Collectors.toMap(CustomerDTO.GetDetails::getId, t -> t));
            }
        }

        List<TechnicianDTO.GetDetails> techDetails = technicianClientService.getTechniciansList(new ArrayList<>(pagedResult.getContent().stream().map(Appointment::getTechnicianId).collect(Collectors.toList())), tenantId);
        if (techDetails != null) {
            techMap = techDetails.stream()
                    .collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));
        }
        List<AppointmentDTO.ListResponse> responseList = new ArrayList<>();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<JobType> jobTypeList = jobTypeRepository.findByUuidAndDeletedFalse(pagedResult.getContent().stream().map(Appointment::getJobTypeId).collect(Collectors.toList()));
        Map<String, String> jobTypeMap = jobTypeList.stream()
                .collect(Collectors.toMap(JobType::getUuid, JobType::getName));
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
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
            dto.setJobTypeName(jobTypeMap.get(appointment.getJobTypeId()));
            dto.setJobTaskId(appointment.getJobTaskId());
            dto.setStartDateTime(appointment.getStartDateTime() != null ? appointment.getStartDateTime().format(dateTimeFormatter) : null);
            dto.setEndDateTime(appointment.getEndDateTime() != null ? appointment.getEndDateTime().format(dateTimeFormatter) : null);
            dto.setTechnicianId(appointment.getTechnicianId());
            dto.setIsActive(appointment.getActive());
            dto.setStatus(appointment.getStatus() != null ? appointment.getStatus() : "Scheduled");
            Job job = jobMap.get(appointment.getJobId());
            if (job != null && customerToNameMap != null) {
                CustomerDTO.GetDetails customer = customerToNameMap.get(job.getCustomerId());
                if (customer != null) {
                    dto.setCustomerName(customer.getName());
                    dto.setLocation(Objects.nonNull(customer.getAddress()) ? customer.getAddress() : null);
                    dto.setLat(Objects.nonNull(customer.getLat()) ? customer.getLat() : null);
                    dto.setLng(Objects.nonNull(customer.getLng()) ? customer.getLng() : null);
                }
            }
            JobMappingTask jobMappingTask = jobMappingTaskMap.get(appointment.getJobTaskId());
            if (jobMappingTask != null) {
                dto.setTaskName(jobMappingTask.getTaskName());
            }
            dto.setAppointmentTypeId(appointment.getAppointmentType() != null ? appointment.getAppointmentType().getUuid() : null);
            dto.setAppointmentTypeName(appointment.getAppointmentType() != null ? appointment.getAppointmentType().getName() : null);
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

    @Override
    public AppointmentDTO.ListResponse getAppointmentById(String id, Long tenantId, String userName) throws CodeException {
        if (TextUtils.isEmpty(id))
            throw new CodeException("appointment id is required", ErrorCode.COMMON);
        Optional<Appointment> appointmentOptional = appointmentRepository.findByUuidAndDeletedFalse(id);
        if (appointmentOptional.isEmpty())
            throw new CodeException("appointment not found", ErrorCode.COMMON);

        Type listType = new TypeToken<List<String>>() {
        }.getType();

        List<Job> byUuidIn = jobRepository.findByJobIdIn(new ArrayList<>(Collections.singleton(appointmentOptional.get().getJobId())));

        List<JobMappingTask> byJobTaskIdIn = jobMappingTaskRepository.findByTaskShowIdIn(new ArrayList<>(Collections.singleton(appointmentOptional.get().getJobTaskId())));
        List<CustomerDTO.GetDetails> customerList = new ArrayList<>();

        if (!byUuidIn.isEmpty()) {
            customerList = adminClientService.getCustomerList(new ArrayList<>(byUuidIn.stream().map(Job::getCustomerId).filter(Objects::nonNull).distinct().collect(Collectors.toList())), tenantId, false);
        }
        List<JobType> jobTypeList = jobTypeRepository.findByUuidAndDeletedFalse(new ArrayList<>(Collections.singleton(appointmentOptional.get().getJobTypeId())));

        Appointment appointment = appointmentOptional.get();
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
        AppointmentDTO.ListResponse dto = new AppointmentDTO.ListResponse();
        dto.setTechnicianName(userName);
        dto.setId(appointment.getUuid());
        dto.setAdditionalNotes(appointment.getAdditionalNotes());
        dto.setJobId(appointment.getJobId());
        dto.setJobTags(new Gson().fromJson(appointment.getJobTags(), listType));
        dto.setJobTypeId(appointment.getJobTypeId());
        dto.setJobTypeName((jobTypeList != null && !jobTypeList.isEmpty()) ? jobTypeList.get(0).getName() : null);
        dto.setJobTaskId(appointment.getJobTaskId());
        dto.setStartDateTime(appointment.getStartDateTime() != null ? appointment.getStartDateTime().format(dateTimeFormatter) : null);
        dto.setEndDateTime(appointment.getEndDateTime() != null ? appointment.getEndDateTime().format(dateTimeFormatter) : null);
        dto.setTechnicianId(appointment.getTechnicianId());
        dto.setIsActive(appointment.getActive());
        if (customerList != null && !customerList.isEmpty()) {
            dto.setCustomerName(customerList.get(0).getName());
            dto.setLocation(Objects.nonNull(customerList.get(0).getAddress()) ? customerList.get(0).getAddress() : null);
            dto.setLat(Objects.nonNull(customerList.get(0).getLat()) ? customerList.get(0).getLat() : null);
            dto.setLng(Objects.nonNull(customerList.get(0).getLng()) ? customerList.get(0).getLng() : null);
        }
        if (byJobTaskIdIn != null && !byJobTaskIdIn.isEmpty()) {
            dto.setTaskName(byJobTaskIdIn.get(0).getTaskName());
        }

        dto.setStatus(appointment.getStatus() != null ? appointment.getStatus() : "Scheduled");
        return dto;
    }

    private void prepareJobTypeSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<Appointment> builder, List<String> jobIds) {
        builder.with(appointmentSpecificationFactory.isEqual("deleted", false));
        if (listRequest.getIsActive() != null) {
            builder.with(appointmentSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (!TextUtils.isEmpty(listRequest.getJobId())) {
            builder.with(appointmentSpecificationFactory.like("jobId", listRequest.getJobId()).or(appointmentSpecificationFactory.isEqual("jobId", listRequest.getJobId())));
        }
        if (jobIds != null && !jobIds.isEmpty()) {
            builder.with(appointmentSpecificationFactory.in("jobId", jobIds));
        }
        if (!TextUtils.isEmpty(listRequest.getAppointmentTypeId())) {
            builder.with(appointmentSpecificationFactory.joinEquals("appointmentType", "uuid", listRequest.getAppointmentTypeId()));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(appointmentSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(appointmentSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }
        if (listRequest.getSearchText() != null && !listRequest.getSearchText().isEmpty()) {
            builder.with(appointmentSpecificationFactory.like("taskId", listRequest.getSearchText()).or(appointmentSpecificationFactory.like("jobId", listRequest.getSearchText()))
                    .or(appointmentSpecificationFactory.isEqual("taskId", listRequest.getSearchText())).or(appointmentSpecificationFactory.isEqual("jobId", listRequest.getSearchText())));
        }

    }

    private void prepareJobTypeSearchFilterForTechnicianId(PageRequest.List listRequest, GenericSpecificationsBuilder<Appointment> builder) {
        builder.with(appointmentSpecificationFactory.isEqual("deleted", false));
        if (listRequest.getIsActive() != null) {
            builder.with(appointmentSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (listRequest.getTechnicianId() != null && !listRequest.getTechnicianId().isEmpty()) {
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
