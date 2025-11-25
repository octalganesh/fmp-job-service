package com.octal.fsm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.clients.TechnicianClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.dto.JobDTO.JobStatusDetail;
import com.octal.fsm.dto.enums.JobUpdateType;
import com.octal.fsm.entities.*;
import com.octal.fsm.entities.enums.TaskAssignedType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.listener.events.SendMailAndPushEvent;
import com.octal.fsm.listener.events.SendMailToTechnicianEvent;
import com.octal.fsm.repositories.*;
import com.octal.fsm.service.DocumentService;
import com.octal.fsm.service.JobService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.transformer.JobTransformer;
import com.octal.fsm.utils.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.lang.reflect.Type;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    private static final Logger logger = LogManager.getLogger(JobServiceImpl.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private QuickBooksCustomerService quickBooksCustomerService;

    @Autowired
    private TechnicianClient technicianClient;

    @Autowired
    private JobTypeRepository jobTypeRepository;

    @Autowired
    private JobTransformer jobTransformer;

    @Autowired
    private SpecificationFactory<Job> jobSpecificationFactory;

    @Autowired
    private SpecificationFactory<JobInvoice> jobInvoiceSpecificationFactory;
    @Autowired
    private SpecificationFactory<JobTaskMappingTechnician> jobTaskMappingTechnicianSpecificationFactory;

    @Autowired
    private SpecificationFactory<JobHistory> jobHistorySpecificationFactory;


    @Autowired
    private SpecificationFactory<JobMappingTask> jobMappingTaskSpecificationFactory;

    @Autowired
    private JobMappingTaskRepository jobMappingTaskRepository;

    @Autowired
    private JobTaskRepository jobTaskRepository;

    @Autowired
    private JobTaskMappingTechnicianRepository jobTaskMappingTechnicianRepository;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private JobTagRepository jobTagRepository;

    @Autowired
    private JobInvoiceRepository jobInvoiceRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private NotificationClient notificationClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    @Autowired
    private JobStatusMasterRepository jobStatusMasterRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private JobNotesRepository jobNotesRepository;

    @Autowired
    private JobService jobService;
    @Autowired
    private DocumentsRepository documentsRepository;
    @Value("${aws.base-url}")
    private String awsS3BaseUrl;
    @Value("${client.feedback.link}")
    private String clientFeedbackLink;

    @Value("${custom.api.task-id}")
    private String baseApiUrl;

    @Override
    public String addJob(JobDTO.Add addJobDTO, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            addJobDTO.setTenantId(!isSuperAdmin ? tenantId : 1L);
            validatedJobDTO(addJobDTO);
            return jobTransformer.transformToEntity(addJobDTO, tenantId, isSuperAdmin); // Using getRecordId() instead of getId()
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public void createUpFrontInvoice(JobDTO.CreateUpFrontInvoiceRequest createUpFrontInvoice, Long tenantId, Boolean isSuperAdmin) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
        Optional<Job> job = jobRepository.findByUuidAndTenantIdAndDeletedFalse(createUpFrontInvoice.getJobId(), tenantId);
        if (job.isEmpty())
            throw new CodeException("Job Not Found", ErrorCode.COMMON);
        String customerRefId = job.get().getCustomerQuickBookId();
        if (TextUtils.isEmpty(customerRefId))
            throw new CodeException("Customer QuickBook Id Not Found", ErrorCode.COMMON);
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        InvoiceRequest.CustomerRef customerRef = new InvoiceRequest.CustomerRef();
        customerRef.setValue(customerRefId);
        invoiceRequest.setCustomerRef(customerRef);
        List<InvoiceRequest.LineItem> lineItems = new ArrayList<>();
        InvoiceRequest.LineItem lineItem = new InvoiceRequest.LineItem();
        lineItem.setAmount(createUpFrontInvoice.getAmount());
        lineItem.setDetailType("SalesItemLineDetail");
        InvoiceRequest.LineItem.SalesItemLineDetail salesItemLineDetail = new InvoiceRequest.LineItem.SalesItemLineDetail();
        InvoiceRequest.LineItem.SalesItemLineDetail.ItemRef itemRef = new InvoiceRequest.LineItem.SalesItemLineDetail.ItemRef();
        itemRef.setName("Project Upfront");
        itemRef.setValue("30");
        salesItemLineDetail.setItemRef(itemRef);
        lineItem.setSalesItemLineDetail(salesItemLineDetail);
        lineItems.add(lineItem);
        invoiceRequest.setLine(lineItems);
        CreateInvoiceDTO invoiceResponse = null;
        if (!TextUtils.isEmpty(createUpFrontInvoice.getDueDate())) {
            invoiceRequest.setDueDate(createUpFrontInvoice.getDueDate());
        }
        if (!TextUtils.isEmpty(createUpFrontInvoice.getNote())) {
            invoiceRequest.setPrivateNote(createUpFrontInvoice.getNote());
        }
        try {
            invoiceResponse = quickBooksCustomerService.createInvoice(invoiceRequest);
            //Send Mail
            JsonNode sendMailResponse = quickBooksCustomerService.sendInvoice(invoiceResponse.getInvoice().getId(), createUpFrontInvoice.getEmail());
            JobInvoice jobInvoice = new JobInvoice();
            jobInvoice.setJobId(createUpFrontInvoice.getJobId());
            jobInvoice.setInvoiceId(invoiceResponse.getInvoice().getId());
            jobInvoice.setAmount(createUpFrontInvoice.getAmount());
            jobInvoice.setSendOnEmail(createUpFrontInvoice.getEmail());
            if (!TextUtils.isEmpty(createUpFrontInvoice.getDueDate())) {
                try {
                    LocalDate dueDate = LocalDate.parse(createUpFrontInvoice.getDueDate());
                    jobInvoice.setDueDate(dueDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Gson gson = new Gson();
            jobInvoice.setNote(createUpFrontInvoice.getNote());
            jobInvoice.setRequestDTO(gson.toJson(invoiceRequest));
            jobInvoice.setResponseDTO(gson.toJson(invoiceResponse));
            jobInvoice.setPaid(false);
            jobInvoice.setInvoiceType(createUpFrontInvoice.getInvoiceType());
            jobInvoiceRepository.save(jobInvoice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public PageItem<JobDTO.InvoiceListResponse> getAllJobInvoices(int page, int size, String sortBy, Boolean order, String jobId, String loggedInUserEmail) throws CodeException {
        Boolean jobExist = jobRepository.existsByUuidAndDeletedFalse(jobId);
        if (!jobExist)
            throw new CodeException("Job Not Found", ErrorCode.COMMON);
        GenericSpecificationsBuilder<JobInvoice> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(order)) {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).descending());
        }
        builder.with(jobInvoiceSpecificationFactory.isEqual("deleted", false));
        builder.with(jobInvoiceSpecificationFactory.isEqual("jobId", jobId));
        Page<JobInvoice> pagedResult = jobInvoiceRepository.findAll(builder.build(), pageable);
        List<JobDTO.InvoiceListResponse> responseList = new ArrayList<>();
        for (JobInvoice jobInvoice : pagedResult.getContent()) {
            JobDTO.InvoiceListResponse dto = new JobDTO.InvoiceListResponse();
            dto.setId(jobInvoice.getUuid());
            dto.setInvoiceId(jobInvoice.getInvoiceId());
            dto.setInvoiceType(jobInvoice.getInvoiceType());
            dto.setAmount(jobInvoice.getAmount());
            dto.setSendOnEmail(jobInvoice.getSendOnEmail());
            dto.setDueDate(jobInvoice.getDueDate() != null ? jobInvoice.getDueDate().toString() : null);
            dto.setNote(jobInvoice.getNote());
            dto.setCreatedAt(jobInvoice.getCreatedAt() != null ? jobInvoice.getCreatedAt().toString() : null);
            dto.setPaid(jobInvoice.getPaid());
            dto.setJobId(jobId);
            responseList.add(dto);
        }
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, page,
                size);
    }

    @Override
    public void updateJobTags(String jobId, JobDTO.UpdateJobTags updateJobTags, String loggedInUserEmail, Long tenantId, Boolean isSuperAdmin) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
        Optional<Job> job = jobRepository.findByUuidAndTenantIdAndDeletedFalse(jobId, tenantId);
        if (job.isEmpty())
            throw new CodeException("Job Not Found", ErrorCode.COMMON);
        if (updateJobTags.getJobTags() == null || updateJobTags.getJobTags().isEmpty())
            throw new CodeException("Job Tags are required", ErrorCode.COMMON);
        List<JobMappingTags> jobMappingTags = job.get().getJobMappingTags();
        for (String jobTagId : updateJobTags.getJobTags()) {
            Boolean jobTagExist = jobTagRepository.existsByUuid(jobTagId);
            if (jobTagExist) {
                JobMappingTags tag = new JobMappingTags();
                tag.setTagId(jobTagId);
                tag.setJob(job.get());
                jobMappingTags.add(tag);
            }
        }
        job.get().setJobMappingTags(jobMappingTags);
        jobRepository.save(job.get());

    }


    @Override
    public PageItem<JobDTO.JobListResponse> getAllJobs(String txt, int page, int size, String sortBy, Boolean order, String jobType, String jobStatus, String jobTag, Double serviceLocationLat, Double serviceLocationLng, String customerType, String fromStartDate, String toStartDate, String location, String loggedInUserEmail, Long tenantId, Boolean isSuperAdmin, String frontOfficeId) throws CodeException {

        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(order)) {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).descending());
        }
        builder.with(jobSpecificationFactory.isEqual("deleted", false));

        builder.with(jobSpecificationFactory.isEqual("tenantId", tenantId));

//        if (org.apache.commons.lang.StringUtils.isNotBlank(listRequest.getSearchText())) {
//            builder.with(jobTagSpecificationFactory.like("name", listRequest.getSearchText()));
//        }
//        if(listRequest.getIsActive()!=null){
//            builder.with(jobTagSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
//        }
        if (!TextUtils.isEmpty(jobType)) {
            builder.with(jobSpecificationFactory.isEqual("jobTypeId", jobType));
        }
        if (!TextUtils.isEmpty(jobStatus)) {
            builder.with(jobSpecificationFactory.isEqual("jobStatus", jobStatus));
        }
        if (!TextUtils.isEmpty(jobTag)) {
            builder.with(jobSpecificationFactory.join("jobMappingTags", "tagId", jobTag));
        }
//        Double serviceLocationLat, Double serviceLocationLng, String customerType,
        if (!TextUtils.isEmpty(serviceLocationLat)) {
            builder.with(jobSpecificationFactory.isEqual("serviceLocationLat", serviceLocationLat));
        }
        if (!TextUtils.isEmpty(serviceLocationLng)) {
            builder.with(jobSpecificationFactory.isEqual("serviceLocationLng", serviceLocationLng));
        }
        if (!TextUtils.isEmpty(customerType)) {
            builder.with(jobSpecificationFactory.isEqual("customerTypeId", customerType));
        }
        if (!TextUtils.isEmpty(fromStartDate)) {
            builder.with(jobSpecificationFactory.isGreaterThanOrEquals("jobStartDate", LocalDate.parse(fromStartDate)));
        }
        if (!TextUtils.isEmpty(toStartDate)) {
            builder.with(jobSpecificationFactory.isLessThanOrEquals("jobEndDate", LocalDate.parse(toStartDate)));
        }
        if (!TextUtils.isEmpty(location)) {
            builder.with(jobSpecificationFactory.like("serviceLocation", location));
        }
        if (!TextUtils.isEmpty(frontOfficeId)) {
            builder.with(jobSpecificationFactory.like("frontOfficeId", frontOfficeId));
        }
        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageable);
        List<JobDTO.JobListResponse> responseList = new ArrayList<>();
        for (Job job : pagedResult.getContent()) {
            JobDTO.JobListResponse dto = new JobDTO.JobListResponse();
            dto.setId(job.getUuid());
            dto.setJobId(job.getJobId());
            dto.setServiceLocation(job.getServiceLocation());
            Optional<JobType> jobTypeOpt = jobTypeRepository.findByUuid(job.getJobTypeId());
            jobTypeOpt.ifPresent(type -> dto.setJobType(type.getName()));
            dto.setJobStartDate(job.getJobStartDate() != null ? job.getJobStartDate().toString() : null);
            dto.setJobEndDate(job.getJobEndDate() != null ? job.getJobEndDate().toString() : null);
            try {
                ApiResponse apiResponse = adminClient.getJobDetailsWithLeadAndCustomerDetails(job.getCustomerId(), job.getLeadSourceId(), loggedInUserEmail, tenantId, isSuperAdmin).getBody();
                if (apiResponse != null && apiResponse.getData() != null) {
                    Gson gson = new Gson();
                    Type customerDetailsStr = new TypeToken<Map<String, String>>() {
                    }.getType();
                    Map<String, String> customerDetails = gson.fromJson(gson.toJson(apiResponse.getData()), customerDetailsStr);
                    if (customerDetails != null) {
                        dto.setCustomerName(customerDetails.get("customerName"));
                        dto.setCustomerType(customerDetails.get("customerType"));
                        dto.setLeadSource(customerDetails.get("leadSourceName"));
                    } else {
                        dto.setCustomerName("");
                        dto.setLeadSource("");
                    }
                } else {
                    dto.setCustomerName("");
                    dto.setLeadSource("");
                }
            } catch (Exception e) {
                dto.setCustomerName("");
            }
            dto.setJobStatus(job.getJobStatus());
            responseList.add(dto);
        }

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, page,
                size);
    }


    @Override
    public JobDTO.Detail getJobById(String id, String loggedInUserEmail, Long tenantId, Boolean isSuperAdmin) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
        Optional<Job> jobOpt = jobRepository.findByUuidAndTenantIdAndDeletedFalse(id, tenantId);
        if (jobOpt.isEmpty())
            throw new CodeException("Job Not Found", ErrorCode.COMMON);
        Job job = jobOpt.get();
        JobDTO.Detail response = new JobDTO.Detail();
        response.setId(id);
        response.setJobStatusMaster(job.getJobStatusMaster());
        response.setJobId(job.getJobId());
        response.setCurrentTaskId(job.getCurrentTaskId());
        Optional<JobType> jobType = jobTypeRepository.findByUuid(job.getJobTypeId());
        if (jobType.isPresent()) {
            response.setJobTypeId(jobType.get().getUuid());
            response.setJobType(jobType.get().getName());
        }
        if (!TextUtils.isEmpty(job.getLeadSourceId()) || !TextUtils.isEmpty(job.getCustomerTypeId()) || !TextUtils.isEmpty(job.getCustomerId())) {
            try {
                ApiResponse apiResponse = adminClient.getJobDetailsForCustomerInfo(job.getCustomerId(), job.getLeadSourceId(), job.getCustomerTypeId(), loggedInUserEmail, tenantId, isSuperAdmin).getBody();
                if (apiResponse != null && apiResponse.getData() != null) {
                    Gson gson = new Gson();
                    Type customerDetailsStr = new TypeToken<Map<String, String>>() {
                    }.getType();
                    Map<String, String> customerDetails = gson.fromJson(gson.toJson(apiResponse.getData()), customerDetailsStr);
                    if (customerDetails != null) {
                        response.setLeadSourceId(job.getLeadSourceId());
                        response.setLeadSource(customerDetails.get("leadSourceName"));
                        response.setCustomerTypeId(job.getCustomerTypeId());
                        response.setCustomerType(customerDetails.get("customerTypeName"));
                        JobDTO.CustomerDetails customerDetailsObject = new JobDTO.CustomerDetails();
                        customerDetailsObject.setCustomerId(job.getCustomerId());
                        customerDetailsObject.setCustomerName(customerDetails.get("customerName"));
                        customerDetailsObject.setEmail(customerDetails.get("customerEmail"));
                        customerDetailsObject.setMobileNumber(customerDetails.get("customerMobile"));
                        customerDetailsObject.setAddress(customerDetails.get("customerAddress"));
                        customerDetailsObject.setLat(!TextUtils.isEmpty(customerDetails.get("customerAddressLat")) && !customerDetails.get("customerAddressLat").equalsIgnoreCase("null") ? Double.parseDouble(customerDetails.get("customerAddressLat")) : 0.0);
                        customerDetailsObject.setLng(!TextUtils.isEmpty(customerDetails.get("customerAddressLng")) && !customerDetails.get("customerAddressLng").equalsIgnoreCase("null") ? Double.parseDouble(customerDetails.get("customerAddressLng")) : 0.0);
                        response.setCustomerDetails(customerDetailsObject);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.setLeadReceivedDate(job.getLeadReceivedDate() != null ? job.getLeadReceivedDate().toString() : null);
        List<JobMappingTags> jobTags = job.getJobMappingTags();
        List<JobTagDTO.Detail> jobTagList = new ArrayList<>();
        for (JobMappingTags tag : jobTags) {
            Optional<JobTag> jobTagOptional = jobTagRepository.findByUuid(tag.getTagId());
            if (jobTagOptional.isPresent()) {
                JobTagDTO.Detail jobTagDTO = new JobTagDTO.Detail();
                jobTagDTO.setId(tag.getTagId());
                jobTagDTO.setTagColor(jobTagOptional.get().getTagColor());
                jobTagDTO.setName(jobTagOptional.get().getName());
                jobTagList.add(jobTagDTO);
            }
        }
        response.setJobTags(jobTagList);
        response.setJobDescription(job.getJobDescription());
        response.setAdditionalNotes(job.getAdditionalNotes());
        response.setJobStartDate(job.getJobStartDate() != null ? job.getJobStartDate().toString() : null);
        response.setJobEndDate(job.getJobEndDate() != null ? job.getJobEndDate().toString() : null);
        response.setServiceLocation(job.getServiceLocation());
        response.setServiceLocationLat(job.getServiceLocationLat());
        response.setServiceLocationLng(job.getServiceLocationLng());
        response.setJobStatus(job.getJobStatus());
        return response;
    }

    @Override
    public PageItem<JobDTO.JobTaskListResponse> getJobTask(int page, int size, String sortBy, Boolean order, String jobId, String loggedInUserEmail) throws CodeException {
        Boolean jobExist = jobRepository.existsByUuidAndDeletedFalse(jobId);
        if (!jobExist)
            throw new CodeException("Job Not Found", ErrorCode.COMMON);
        GenericSpecificationsBuilder<JobMappingTask> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(order)) {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).descending());
        }
        builder.with(jobMappingTaskSpecificationFactory.isEqual("deleted", false));
        if (!TextUtils.isEmpty(jobId)) {
            builder.with(jobMappingTaskSpecificationFactory.join("job", "uuid", jobId));
        }
        List<JobDTO.JobTaskListResponse> responseList = new ArrayList<>();
        Page<JobMappingTask> pagedResult = jobMappingTaskRepository.findAll(builder.build(), pageable);
        for (JobMappingTask jobMappingTask : pagedResult) {
            JobDTO.JobTaskListResponse dto = new JobDTO.JobTaskListResponse();
            Optional<JobTask> jobTask = jobTaskRepository.findByUuid(jobMappingTask.getTaskId());
            if (jobTask.isPresent()) {
                dto.setId(jobMappingTask.getUuid());
                dto.setTaskId(jobTask.get().getUuid());
                dto.setTaskShowId(jobMappingTask.getTaskShowId());
                dto.setTaskName(jobTask.get().getName());
                dto.setTaskDescription(jobTask.get().getDescription());
                dto.setAssignedType(jobMappingTask.getAssignType());
                dto.setSequenceNumber(jobTask.get().getSequence());
                Optional<JobTaskMappingTechnician> jobTaskMappingTechnician = jobTaskMappingTechnicianRepository.findByJobTaskMappingId(jobMappingTask.getUuid());
                if (jobTaskMappingTechnician.isPresent()) {
                    dto.setCreatedAt(jobTaskMappingTechnician.get().getCreatedAt() != null ? jobTaskMappingTechnician.get().getCreatedAt().toString() : null);
                    dto.setTaskStatus(jobTaskMappingTechnician.get().getTaskStatus());
                    ApiResponse technicianResponse = technicianClient.getTechnicianById(jobTaskMappingTechnician.get().getTechnicianId(), loggedInUserEmail).getBody();
                    if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200") && technicianResponse.getData() != null) {
                        try {
                            Gson gson = new Gson();
                            TechnicianDTO.GetDetails technicianDetails = gson.fromJson(gson.toJson(technicianResponse.getData()), TechnicianDTO.GetDetails.class);
                            dto.setTechnicianName(technicianDetails.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    dto.setTaskStatus("NOT ASSIGNED");
                }
                responseList.add(dto);
            }
        }
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, page,
                size);
    }

    @Override
    public void assignJobToTechnician(JobDTO.AssignJobToTechnician assignJobToTechnician, Long tenantId, Boolean isSuperAdmin, String loggedInUserEmail) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
        Boolean jobExist = jobRepository.existsByUuidAndTenantIdAndDeletedFalse(assignJobToTechnician.getJobId(), tenantId);
        if (!jobExist)
            throw new CodeException("Job Not Found", ErrorCode.COMMON);
        Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(assignJobToTechnician.getJobTaskMappingId());
        if (jobMappingTask.isEmpty())
            throw new CodeException("Job Task Mapping Not Found", ErrorCode.COMMON);
        Boolean taskExist = jobTaskRepository.existsByUuid(jobMappingTask.get().getTaskId());
        if (!taskExist)
            throw new CodeException("Job Task Not Found", ErrorCode.COMMON);
        ApiResponse technicianResponse = technicianClient.getTechnicianById(assignJobToTechnician.getTechnicianId(), loggedInUserEmail).getBody();
        if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200")) {
            JobTaskMappingTechnician jobTaskMappingTechnician = new JobTaskMappingTechnician();
            Optional<JobTaskMappingTechnician> jobTaskMappingToTechnician = jobTaskMappingTechnicianRepository.findByJobTaskMappingId(assignJobToTechnician.getJobTaskMappingId());
            if (jobTaskMappingToTechnician.isPresent()) {
                //Todo need to create log for all assignment and reassignment of technician
                jobTaskMappingToTechnician.get().setTechnicianId(assignJobToTechnician.getTechnicianId());
                jobTaskMappingToTechnician.get().setNote(assignJobToTechnician.getNote());
                if (!TextUtils.isEmpty(assignJobToTechnician.getStartDate())) {
                    try {
                        LocalDate startDate = LocalDate.parse(assignJobToTechnician.getStartDate());
                        jobTaskMappingToTechnician.get().setStartDate(startDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!TextUtils.isEmpty(assignJobToTechnician.getEndDate())) {
                    try {
                        LocalDate endDate = LocalDate.parse(assignJobToTechnician.getEndDate());
                        jobTaskMappingToTechnician.get().setEndDate(endDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (assignJobToTechnician.getDocuments() != null && !assignJobToTechnician.getDocuments().isEmpty()) {
                    Gson gson = new Gson();
                    jobTaskMappingToTechnician.get().setDocuments(gson.toJson(assignJobToTechnician.getDocuments()));
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                JobTaskMappingTechnician save = jobTaskMappingTechnicianRepository.save(jobTaskMappingToTechnician.get());

                if (assignJobToTechnician.getStartDateTime() != null) {
                    LocalDateTime ldt = LocalDateTime.parse(assignJobToTechnician.getStartDateTime(), formatter);
                    Time sqlTime = Time.valueOf(ldt.toLocalTime());
                    jobTaskMappingToTechnician.get().setStartTime(sqlTime);
                }

                if (assignJobToTechnician.getEndDateTime() != null) {
                    LocalDateTime ldt = LocalDateTime.parse(assignJobToTechnician.getEndDateTime(), formatter);
                    Time sqlTime = Time.valueOf(ldt.toLocalTime());
                    jobTaskMappingToTechnician.get().setEndTime(sqlTime);
                }
                Gson gson = new Gson();
                if (assignJobToTechnician.getDocuments() != null && !assignJobToTechnician.getDocuments().isEmpty()) {
                    List<String> documentsWithUrl = assignJobToTechnician.getDocuments().stream()
                            .map(doc -> awsS3BaseUrl + doc)  // Prepending AWS base URL
                            .collect(Collectors.toList());
                    jobTaskMappingToTechnician.get().setDocuments(gson.toJson(documentsWithUrl));
                }
                try {
                    applicationEventPublisher.publishEvent(new SendMailToTechnicianEvent(assignJobToTechnician, save, loggedInUserEmail, tenantId, isSuperAdmin));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                jobTaskMappingTechnician.setJobTaskMappingId(jobMappingTask.get().getUuid());
                jobTaskMappingTechnician.setTechnicianId(assignJobToTechnician.getTechnicianId());
                jobTaskMappingTechnician.setTaskStatus("ASSIGNED");
                jobTaskMappingTechnician.setNote(assignJobToTechnician.getNote());
                if (!TextUtils.isEmpty(assignJobToTechnician.getStartDate())) {
                    try {
                        LocalDate startDate = LocalDate.parse(assignJobToTechnician.getStartDate());
                        jobTaskMappingTechnician.setStartDate(startDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!TextUtils.isEmpty(assignJobToTechnician.getEndDate())) {
                    try {
                        LocalDate endDate = LocalDate.parse(assignJobToTechnician.getEndDate());
                        jobTaskMappingTechnician.setEndDate(endDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Gson gson = new Gson();
                if (assignJobToTechnician.getDocuments() != null && !assignJobToTechnician.getDocuments().isEmpty()) {
                    List<String> documentsWithUrl = assignJobToTechnician.getDocuments().stream()
                            .map(doc -> awsS3BaseUrl + doc)  // Prepending AWS base URL
                            .collect(Collectors.toList());
                    jobTaskMappingTechnician.setDocuments(gson.toJson(documentsWithUrl));
                }
                JobTaskMappingTechnician JobTaskMappingTechnician = jobTaskMappingTechnicianRepository.save(jobTaskMappingTechnician);

                // Save attached documents in DB
                JobTaskMappingTechnician.setDocuments(gson.toJson(assignJobToTechnician.getDocuments()));
                try {
                    applicationEventPublisher.publishEvent(new SendMailToTechnicianEvent(assignJobToTechnician, JobTaskMappingTechnician, loggedInUserEmail, tenantId, isSuperAdmin));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//                throw new CodeException("Job Task Already Assigned to Technician", ErrorCode.COMMON);
        } else {
            throw new CodeException("Technician Not Found", ErrorCode.COMMON);
        }
    }

    @Override
    public void updateAssignedTaskWithDocumentType(String jobTaskMappingId, JobDTO.UpdateAssignedTaskWithDocumentType updateAssignedTaskWithDocumentType, String loggedInUserEmail) throws CodeException {
        Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(jobTaskMappingId);
        if (jobMappingTask.isEmpty())
            throw new CodeException("Job Task Mapping Not Found", ErrorCode.COMMON);
        jobMappingTask.get().setDocumentTypeId(new Gson().toJson(updateAssignedTaskWithDocumentType.getDocumentTypeId()));
        jobMappingTaskRepository.save(jobMappingTask.get());
    }

    @Override
    public void updateJobTask(String technicianId, String taskId, String note, String userName) throws CodeException {
        Optional<JobTaskMappingTechnician> jobTaskMappingTechnician = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(taskId);
        if (jobTaskMappingTechnician.isPresent()) {
            JobTaskMappingTechnician taskMappingTechnician = jobTaskMappingTechnician.get();
            if (taskMappingTechnician.getTaskStatus().equalsIgnoreCase("COMPLETED")) {
                throw new CodeException("Task Already Completed", ErrorCode.BAD_REQUEST);
            }
            if (TextUtils.isEmpty(note))
                throw new CodeException("note cannot be empty", ErrorCode.BAD_REQUEST);
            taskMappingTechnician.setTechnicianNote(note);
            jobTaskMappingTechnicianRepository.save(taskMappingTechnician);
        } else {
            throw new CodeException("Task Not Found", ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public void addDrawingToJobTask(String technicianId, String taskId, JobDTO.TaskDrawingRequest taskDrawingRequest, String userName) throws CodeException {
        Optional<JobTaskMappingTechnician> jobTaskMappingTechnician = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(taskId);
        if (jobTaskMappingTechnician.isPresent()) {
            JobTaskMappingTechnician taskMappingTechnician = jobTaskMappingTechnician.get();
            if (TextUtils.isEmpty(taskDrawingRequest.getDrawingJson()))
                throw new CodeException("Drawing Json cannot be empty", ErrorCode.BAD_REQUEST);
            taskMappingTechnician.setDrawingJson(taskDrawingRequest.getDrawingJson());
            if (!TextUtils.isEmpty(taskDrawingRequest.getDrawingFileUrl()))
                taskMappingTechnician.setDrawingImage(awsS3BaseUrl + taskDrawingRequest.getDrawingFileUrl());
            jobTaskMappingTechnicianRepository.save(taskMappingTechnician);
        } else {
            throw new CodeException("Task Not Found", ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public void addJobStatus(List<JobDTO.AddJobStatus> addJobStatus, Long tenantId, boolean isSuperAdmin, String userName) {
        if (isSuperAdmin)
            tenantId = 1L;
        List<JobStatusMaster> jobStatusMasterList = new ArrayList<>();
        for (JobDTO.AddJobStatus dto : addJobStatus) {
            JobStatusMaster jobStatusMaster = new JobStatusMaster();
            jobStatusMaster.setName(dto.getName());
            jobStatusMaster.setSequenceOrder(dto.getSequenceOrder());
            jobStatusMaster.setTenantId(tenantId);
            jobStatusMasterList.add(jobStatusMaster);
        }

        jobStatusMasterRepository.saveAll(jobStatusMasterList);
    }


    @Override
    public void leaveOrReAssignJob(JobDTO.@Valid LeaveJob leaveJob, Long tenantId, boolean isSuperAdmin, String userName) throws CodeException {

        if (leaveJob.getJobUpdateType().equals(JobUpdateType.CANCEL) && TextUtils.isEmpty(leaveJob.getReasonForLeave())) {
            throw new CodeException("Please provide a cancel Reason", ErrorCode.COMMON);
        }

        if (!TextUtils.isEmpty(leaveJob.getJobId())) {
            Job job = new Job();
            if (leaveJob.getJobUpdateType().equals(JobUpdateType.CANCEL)) {
                Optional<Job> jobOptional = jobRepository.findByUuidAndTenantIdAndFrontOfficeIdAndDeletedFalse(leaveJob.getJobId(), tenantId, leaveJob.getFrontOfficeUserId());
                if (jobOptional.isPresent()) {
                    job = jobOptional.get();
                    job.setFrontOfficeId("");
                } else {
                    throw new CodeException("Job not found", ErrorCode.COMMON);
                }
                jobRepository.save(job);
                JobHistory jobHistory = new JobHistory();
                jobHistory.setJobId(leaveJob.getJobId());
                jobHistory.setReason(leaveJob.getReasonForLeave());
                jobHistory.setFrontOfficeId(leaveJob.getFrontOfficeUserId());
                jobHistory.setTenantId(tenantId);
                jobHistory.setActive(false);
                jobHistory.setFrontOfficeName(leaveJob.getFrontOfficeUserName());
                jobHistory.setUpdatedAt(LocalDateTime.now());
                jobHistoryRepository.save(jobHistory);
            } else {

                Optional<Job> jobOptional = jobRepository.findByUuidAndTenantIdAndFrontOfficeIdAndDeletedFalse(leaveJob.getJobId(), tenantId, "");
                if (jobOptional.isPresent()) {
                    job = jobOptional.get();
                    job.setFrontOfficeId(leaveJob.getFrontOfficeUserId());
                } else {
                    throw new CodeException("Job not found", ErrorCode.COMMON);
                }
                jobRepository.save(job);
                JobHistory jobHistory = new JobHistory();
                jobHistory.setJobId(leaveJob.getJobId());
                jobHistory.setReason(leaveJob.getReasonForLeave());
                jobHistory.setFrontOfficeId(leaveJob.getFrontOfficeUserId());
                jobHistory.setFrontOfficeName(leaveJob.getFrontOfficeUserName());
                jobHistory.setTenantId(tenantId);
                jobHistory.setActive(true);
                jobHistory.setUpdatedAt(LocalDateTime.now());
                jobHistoryRepository.save(jobHistory);


            }
        } else {
            throw new CodeException("Job id cannot be empty", ErrorCode.COMMON);

        }
    }

    @Override
    public PageItem<JobDTO.JobHistoryDTO> jobLeaveReassignHistory(int page, int size, String sortBy, Boolean order, String fromStartDate, String toStartDate, String jobId, String userName, Long tenantId, boolean isSuperAdmin) {

        GenericSpecificationsBuilder<JobHistory> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(order)) {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).descending());
        }
        builder.with(jobHistorySpecificationFactory.isEqual("deleted", false));

        builder.with(jobHistorySpecificationFactory.isEqual("tenantId", tenantId));
        if (!TextUtils.isEmpty(jobId)) {
            builder.with(jobHistorySpecificationFactory.isEqual("jobId", jobId));
        }
        if (!TextUtils.isEmpty(fromStartDate)) {
            builder.with(jobHistorySpecificationFactory.isGreaterThanOrEquals("jobStartDate", LocalDate.parse(fromStartDate)));
        }
        if (!TextUtils.isEmpty(toStartDate)) {
            builder.with(jobHistorySpecificationFactory.isLessThanOrEquals("jobEndDate", LocalDate.parse(toStartDate)));
        }

        Page<JobHistory> pagedResult = jobHistoryRepository.findAll(builder.build(), pageable);
        List<JobDTO.JobHistoryDTO> responseList = new ArrayList<>();
        for (JobHistory jobHistory : pagedResult.getContent()) {
            JobDTO.JobHistoryDTO dto = new JobDTO.JobHistoryDTO();
            dto.setId(jobHistory.getUuid());
            dto.setJobId(jobHistory.getJobId());
            dto.setJobId(jobHistory.getJobId());
            dto.setReason(jobHistory.getReason());
            dto.setTenantId(jobHistory.getTenantId());
            dto.setCreatedAt(jobHistory.getCreatedAt());
            dto.setFrontOfficeId(jobHistory.getFrontOfficeId());
            dto.setUpdatedAt(jobHistory.getUpdatedAt());
            dto.setIsActive(jobHistory.getActive());
            dto.setFrontOfficeName(jobHistory.getFrontOfficeName());
            responseList.add(dto);
        }

        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, page,
                size);
    }
    public void validatedJobDTO(JobDTO.Add addJobDTO) throws CodeException {
        if (addJobDTO.getCustomerDetails() == null)
            throw new CodeException("Customer Details are required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getCustomerDetails().getCustomerId())) {
            if (TextUtils.isEmpty(addJobDTO.getCustomerDetails().getCustomerName())) {
                throw new CodeException("Customer Name is required", ErrorCode.COMMON);
            }
            if (TextUtils.isEmpty(addJobDTO.getCustomerDetails().getEmail())) {
                throw new CodeException("Customer Email is required", ErrorCode.COMMON);
            }
            if (TextUtils.isEmpty(addJobDTO.getCustomerDetails().getMobileNumber())) {
                throw new CodeException("Customer Mobile Number is required", ErrorCode.COMMON);
            }
            if (TextUtils.isEmpty(addJobDTO.getCustomerDetails().getAddress())) {
                throw new CodeException("Customer Address is required", ErrorCode.COMMON);
            }
        }
        if (TextUtils.isEmpty(addJobDTO.getJobTypeId()))
            throw new CodeException("Job Type is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getServiceLocation()))
            throw new CodeException("Service Location is required", ErrorCode.COMMON);
        if (addJobDTO.getServiceLocationLat() == null)
            throw new CodeException("Service Location Latitude is required", ErrorCode.COMMON);
        if (addJobDTO.getServiceLocationLng() == null)
            throw new CodeException("Service Location Longitude is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getJobStatus()))
            throw new CodeException("Job Status is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getCustomerTypeId()))
            throw new CodeException("Customer Type is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getJobDescription()))
            throw new CodeException("Job Description is required", ErrorCode.COMMON);
        if (addJobDTO.getJobTaskId() == null || addJobDTO.getJobTaskId().isEmpty())
            throw new CodeException("Job Tasks are required.", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getLeadReceivedDate()))
            throw new CodeException("Lead Received Date is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getJobStartDate()))
            throw new CodeException("Job Start Date is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getJobEndDate()))
            throw new CodeException("Job End Date is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getLeadSourceId()))
            throw new CodeException("Lead Source is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getBudget()))
            throw new CodeException("Budget is required", ErrorCode.COMMON);
        if (addJobDTO.getJobTags() == null || addJobDTO.getJobTags().isEmpty())
            throw new CodeException("At least one Job Tag is required", ErrorCode.COMMON);
//        if(TextUtils.isEmpty(addJobDTO.getTechnicianId()))
//            throw new CodeException("Technician is required", ErrorCode.COMMON);
    }

    @Override
    public JobDTO.DetailsForTechnician getJobTaskDetailsForTechnician(String technicianId, String taskId, String
            userName, Long tenantId) throws CodeException {
        Optional<JobTaskMappingTechnician> taskMappingOpt = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(taskId);
//        if (taskMappingOpt.isEmpty()) {
//            return new PageItem<>()
//        }
        JobTaskMappingTechnician taskMapping = taskMappingOpt.get();
        List<JobDTO.DetailsForTechnician> detailsList = buildTechnicianJobTaskDetails(List.of(taskMapping), "", userName, tenantId);
        return detailsList.isEmpty() ? null : detailsList.get(0);
    }

    @Override
    public void updateJobTaskStatus(String technicianId, String taskId, String status, String note, String signature, String userName, Long tenantId) throws CodeException {
        Optional<JobTaskMappingTechnician> jobTaskMappingTechnician = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(taskId);
        if (jobTaskMappingTechnician.isPresent()) {
            JobTaskMappingTechnician taskMappingTechnician = jobTaskMappingTechnician.get();
            if (taskMappingTechnician.getTaskStatus().equalsIgnoreCase("COMPLETED")) {
                throw new CodeException("Task Already Completed", ErrorCode.BAD_REQUEST);
            }
            taskMappingTechnician.setTaskStatus(status);
            if (taskMappingTechnician.getTaskStatus().equalsIgnoreCase("cancelled")) {
                if (TextUtils.isEmpty(note)) {
                    throw new CodeException("Cancel Reason is required to cancel the task", ErrorCode.BAD_REQUEST);
                }
                taskMappingTechnician.setCancelReason(note);
            } else if (!TextUtils.isEmpty(note)) {
                taskMappingTechnician.setTechnicianNote(note);
            }
            if (status.equalsIgnoreCase("COMPLETED")) {
                if (TextUtils.isEmpty(signature))
                    throw new CodeException("Customer Signature is required to complete the task", ErrorCode.BAD_REQUEST);
                taskMappingTechnician.setSignature(awsS3BaseUrl + signature);
                taskMappingTechnician.setSignatureDateTime(LocalDateTime.now());
                Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(jobTaskMappingTechnician.get().getJobTaskMappingId());

                if (jobMappingTask.isPresent()) {
                    JobMappingTask currentTask = jobMappingTask.get();
                    int currentSeq = currentTask.getTaskSequence();
                    Optional<Job> jobOptional = jobRepository.findByUuidAndDeletedFalse(jobMappingTask.get().getJob().getUuid());
                    List<JobMappingTask> jobMappingTasks = jobMappingTaskRepository.findByJobOrderByTaskSequenceAsc(jobOptional.get());
                    if (!jobMappingTasks.isEmpty()) {
                        JobMappingTask nextTask = null;
                        for (JobMappingTask task : jobMappingTasks) {
                            if (task.getTaskSequence() > currentSeq) {
                                nextTask = task;
                                break;
                            }
                        }
                        if (nextTask != null) {
                            jobOptional.get().setCurrentTaskId(nextTask.getUuid());
                            List<JobStatusMaster> jobStatus = jobStatusMasterRepository.findByName(nextTask.getJobTaskStatus());
                            if (!jobStatus.isEmpty()) {
                                jobOptional.get().setJobStatusMaster(jobStatus.get(0));
                                jobOptional.get().setJobStatus(jobStatus.get(0).getName());
                            }

                        } else {
                            jobOptional.get().setCurrentTaskId(null);
                        }
                        jobRepository.save(jobOptional.get());
                    }
                }

            }
            jobTaskMappingTechnicianRepository.save(taskMappingTechnician);
            try {
                applicationEventPublisher.publishEvent(new SendMailAndPushEvent(taskMappingTechnician, tenantId, userName));
            } catch (Exception exception) {
                throw new CodeException("Error while sending mail and notification", ErrorCode.COMMON);
            }
        } else {
            throw new CodeException("Task Not Found", ErrorCode.BAD_REQUEST);
        }
    }

    private List<JobDTO.DetailsForTechnician> buildTechnicianJobTaskDetails
            (List<JobTaskMappingTechnician> taskMappings, String txt, String loggedInUserEmail, Long tenantId) {
        List<JobDTO.DetailsForTechnician> responseList = new ArrayList<>();

        for (JobTaskMappingTechnician taskMapping : taskMappings) {
            Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(taskMapping.getJobTaskMappingId());
            if (jobMappingTask.isPresent()) {
                Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(jobMappingTask.get().getJob().getUuid());
                Optional<JobTask> jobTask = jobTaskRepository.findByUuid(jobMappingTask.get().getTaskId());
                JobDTO.DetailsForTechnician details = new JobDTO.DetailsForTechnician();

                if (job.isPresent() && jobTask.isPresent()) {
                    if (!TextUtils.isEmpty(job.get().getFrontOfficeId())) {
                        ResponseEntity<ApiResponse> frontOfficeResponse = adminClient.getFrontOfficeById(job.get().getFrontOfficeId(), tenantId);
                        if (frontOfficeResponse != null && frontOfficeResponse.getBody() != null && frontOfficeResponse.getBody().getData() != null) {
                            Gson gson = new Gson();
                            String stringResponse = gson.toJson(frontOfficeResponse.getBody().getData());
                            FrontOfficeStaffDTO.list frontOfficeResponseData = gson.fromJson(stringResponse, FrontOfficeStaffDTO.list.class);
                            if (frontOfficeResponseData != null) {
                                details.setFrontOfficeName(frontOfficeResponseData.getName());
                                details.setFrontOfficeId(frontOfficeResponseData.getId());
                            }
                        }
                    }
//                    // ✅ Apply search filter on jobId
//                    if (txt != null && !job.get().getJobId().toLowerCase().contains(txt.toLowerCase())) {
//                        continue; // skip this record if jobId does not match
//                    }
                    String jobId = job.get().getJobId() != null ? job.get().getJobId().toLowerCase() : "";
                    String taskShowId = jobMappingTask.get().getTaskShowId() != null ? jobMappingTask.get().getTaskShowId().toLowerCase() : "";

                    // ✅ Unified and safer search filter
                    if (txt != null && !txt.trim().isEmpty()) {
                        String searchTxt = txt.trim().toLowerCase();

                        // Check if the search text matches either Job ID or Task ID
                        boolean matchesJobId = jobId.contains(searchTxt);
                        boolean matchesTaskId = taskShowId.contains(searchTxt);

                        // Skip this record if neither field matches
                        if (!matchesJobId && !matchesTaskId) {
                            continue;
                        }
                    }
                    ResponseEntity<ApiResponse> response = adminClient.getFeedbackByJobTaskId(jobMappingTask.get().getTaskShowId(), null);
                    if (response != null && response.getBody() != null && response.getBody().getData() != null) {
                        Gson gson = new Gson();
                        String stringResponse = gson.toJson(response.getBody().getData());
                        JobDTO.CustomerFeedbackResponse customerFeedbackResponse = gson.fromJson(stringResponse, JobDTO.CustomerFeedbackResponse.class);
                        if (customerFeedbackResponse != null) {
                            details.setCustomerFeedbackResponse(customerFeedbackResponse);
                        }
                    }
                    details.setId(taskMapping.getUuid());
                    details.setTaskName(jobTask.get().getName());
                    details.setNote(taskMapping.getTechnicianNote());
                    details.setFrontOfficeNote(taskMapping.getNote());
                    String customerFeedbackLink = clientFeedbackLink
                            .replace("<jobId>", job.get().getJobId())
                            .replace("<taskId>", jobMappingTask.get().getTaskShowId())
                            .replace("<technicianId>", taskMapping.getTechnicianId())
                            .replace("<customerId>", job.get().getCustomerId());
                    details.setClientFeedbackUrl(customerFeedbackLink);
                    if (!TextUtils.isEmpty(taskMapping.getSignature()))
                        details.setSignature(taskMapping.getSignature());
                    if (!TextUtils.isEmpty(taskMapping.getCancelReason()))
                        details.setCancelReason(taskMapping.getCancelReason());
                    if (!TextUtils.isEmpty(taskMapping.getDrawingJson()))
                        details.setDrawingJsonData(taskMapping.getDrawingJson());
                    if (!TextUtils.isEmpty(taskMapping.getDrawingImage()))
                        details.setDrawingImage(taskMapping.getDrawingImage());
                    details.setTaskId(jobMappingTask.get().getTaskShowId());
                    details.setJobId(job.get().getJobId());
                    details.setJobNote(job.get().getAdditionalNotes());
                    details.setJobStartDate(job.get().getJobStartDate().toString());
                    details.setJobEndDate(job.get().getJobEndDate().toString());
                    details.setTaskDescription(jobTask.get().getDescription());
                    details.setJobTitle(jobTask.get().getName());
                    details.setJobDescription(job.get().getJobDescription());
                    details.setStartDate(taskMapping.getStartDate() != null ? taskMapping.getStartDate().toString() : null);
                    details.setEndDate(taskMapping.getEndDate() != null ? taskMapping.getEndDate().toString() : null);
                    details.setServiceLocationLat(job.get().getServiceLocationLat());
                    details.setServiceLocationLng(job.get().getServiceLocationLng());
                    if (taskMapping.getTaskStatus().equalsIgnoreCase("ASSIGNED")) {
                        details.setStatus("NEW");
                    } else {
                        details.setStatus(taskMapping.getTaskStatus());
                    }

                    // Get job type
                    Optional<JobType> jobType = jobTypeRepository.findByUuid(job.get().getJobTypeId());
                    jobType.ifPresent(type -> details.setJobType(type.getName()));

                    // Get customer details
                    try {
                        ApiResponse customerResponse = adminClient.getCustomerById(job.get().getCustomerId()).getBody();
                        if (customerResponse != null && customerResponse.getStatus() != null && customerResponse.getStatus().equalsIgnoreCase("200") && customerResponse.getData() != null) {
                            Gson gson = new Gson();
                            CustomerDTO.GetDetails customerDetails = gson.fromJson(gson.toJson(customerResponse.getData()), CustomerDTO.GetDetails.class);
                            details.setCustomerId(customerDetails.getId());
                            details.setCustomerName(customerDetails.getName());
                            details.setEmail(customerDetails.getEmail());
                            details.setMobileNumber(customerDetails.getMobileNumber());
                            details.setLocation(customerDetails.getAddress());
                        }
                    } catch (Exception e) {
                        logger.error("Error fetching customer details: {}", e.getMessage());
                    }

                    List<String> jobTags = new ArrayList<>();
                    List<JobMappingTags> jobMappingTags = job.get().getJobMappingTags();
                    for (JobMappingTags mappingTag : jobMappingTags) {
                        Optional<JobTag> tag = jobTagRepository.findByUuid(mappingTag.getTagId());
                        tag.ifPresent(jobTag -> jobTags.add(jobTag.getName()));
                    }
                    details.setJobTags(jobTags);
                    List<Documents> jobDocuments = documentsRepository.findByAttachTypeId(job.get().getJobId());
                    if (!jobDocuments.isEmpty()) {
                        for (Documents documents : jobDocuments) {
                            JobDTO.Document document = new JobDTO.Document();
                            document.setFile(documents.getDocumentUrl());
                            document.setFileType(documents.getFileType());
                            document.setFileName(documents.getFileName());
                            if (documents.getThumbnail() != null)
                                document.setThumbnail(documents.getThumbnail());
                            document.setDocumentTypeId(documents.getDocumentTypeId());
                            details.getJobUploadedDocuments().add(document);
                        }
                    }
                    // Get uploaded documents
                    List<JobDTO.Document> documents = new ArrayList<>();
                    if (!TextUtils.isEmpty(taskMapping.getDocuments())) {
                        try {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<String>>() {
                            }.getType();
                            List<String> documentList = gson.fromJson(taskMapping.getDocuments(), listType);
                            for (String doc : documentList) {
                                JobDTO.Document document = new JobDTO.Document();
                                document.setFile(doc);
                                document.setFileType(TextUtils.getFileTypeFromFileUrl(doc));
                                document.setFileName(TextUtils.getFileNameFromFileUrl(doc));
                                documents.add(document);
                            }
                        } catch (Exception e) {
                            logger.error("Error parsing documents: {}", e.getMessage());
                        }
                    }
                    List<Documents> documentsList = documentsRepository.findByAttachTypeId(taskMapping.getJobTaskMappingId());
                    if (!documentsList.isEmpty()) {
                        for (Documents documents1 : documentsList) {
                            JobDTO.Document document = new JobDTO.Document();
                            document.setFile(documents1.getDocumentUrl());
                            document.setFileType(documents1.getFileType());
                            document.setFileName(documents1.getFileName());
                            if (documents1.getThumbnail() != null)
                                document.setThumbnail(documents1.getThumbnail());
                            documents.add(document);
                        }
                    }
                    details.setUploadedDocuments(documents);
                    if (!taskMapping.getHtmlFormPages().isEmpty()) {
                        List<HTMLFormDTO.Details> list = new ArrayList<>();
                        for (HTMLFormPage htmlFormPage : taskMapping.getHtmlFormPages()) {
                            HTMLFormDTO.Details htmlFormDTO = new HTMLFormDTO.Details();
                            htmlFormDTO.setId(htmlFormPage.getUuid());
                            htmlFormDTO.setContent(htmlFormPage.getContent());
                            htmlFormDTO.setActive(htmlFormPage.getActive());
                            htmlFormDTO.setCreatedAt(htmlFormPage.getCreatedAt().toString());
                            list.add(htmlFormDTO);
                        }
                        details.setFormList(list);
                    }
                    responseList.add(details);
                }
            }
        }

        return responseList;
    }

    @Override
    public PageItem<JobDTO.DetailsForTechnician> getJobTasksForTechnician(
            JobDTO.JobFilterRequest filterRequest,
            String technicianId,
            String loggedInUserEmail,Long tenantId) throws CodeException {
        try {
            GenericSpecificationsBuilder<JobTaskMappingTechnician> builder = new GenericSpecificationsBuilder<>();
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("deleted", false));
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("technicianId", technicianId));

            int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
            int limit = filterRequest.getLimit() != null ? filterRequest.getLimit() : 10;
            Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

            // ✅ Filter by task status
            if (!TextUtils.isEmpty(filterRequest.getStatus())) {
                if (filterRequest.getStatus().equalsIgnoreCase("new"))
                    builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("taskStatus", "ASSIGNED"));
                else if (filterRequest.getStatus().equalsIgnoreCase("ongoing")) {
                    Set<String> statusList = Set.of("ENROUTE", "ARRIVED", "INPROGRESS");
                    builder.with(jobTaskMappingTechnicianSpecificationFactory.fieldIn("taskStatus", statusList));
                } else
                    builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("taskStatus", filterRequest.getStatus()));
            }

            // ✅ Date filters
            if (!TextUtils.isEmpty(filterRequest.getStartDate())) {
                LocalDate startDate = LocalDate.parse(filterRequest.getStartDate());
                builder.with(jobTaskMappingTechnicianSpecificationFactory.isGreaterThanOrEquals("startDate", startDate));
            }

            if (!TextUtils.isEmpty(filterRequest.getEndDate())) {
                LocalDate endDate = LocalDate.parse(filterRequest.getEndDate());
                builder.with(jobTaskMappingTechnicianSpecificationFactory.isLessThanOrEquals("endDate", endDate));
            }

            // ✅ Execute base query
            Page<JobTaskMappingTechnician> pagedResult = jobTaskMappingTechnicianRepository.findAll(builder.build(), pageable);


            // ✅ Filter by job type or job tag (after fetching)
            List<JobTaskMappingTechnician> filteredList = pagedResult.getContent().stream()
                    .filter(taskMapping -> {
                        Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(taskMapping.getJobTaskMappingId());
                        if (jobMappingTask.isEmpty()) return false;

                        Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(jobMappingTask.get().getJob().getUuid());
                        if (job.isEmpty()) return false;

                        boolean match = true;

                        // ✅ Filter by Job Type array
                        if (filterRequest.getJobType() != null && !filterRequest.getJobType().isEmpty()) {
                            match = match && filterRequest.getJobType().contains(job.get().getJobTypeId());
                        }

                        // ✅ Filter by Job Tag array
                        if (filterRequest.getJobTag() != null && !filterRequest.getJobTag().isEmpty()) {
                            List<String> jobTagIds = job.get().getJobMappingTags()
                                    .stream()
                                    .map(JobMappingTags::getTagId)
                                    .collect(Collectors.toList());

                            boolean hasCommonTag = jobTagIds.stream()
                                    .anyMatch(tagId -> filterRequest.getJobTag().contains(tagId));

                            match = match && hasCommonTag;
                        }

                        return match;
                    })
                    .collect(Collectors.toList());

            List<JobDTO.DetailsForTechnician> responseList = buildTechnicianJobTaskDetails(filteredList, filterRequest.getTxt(), loggedInUserEmail,tenantId);

            return new PageItem<>(pagedResult.getTotalPages(), responseList.size(), responseList, page, limit);

        } catch (Exception e) {
            logger.error("Error getting job tasks for technician: {}", e.getMessage(), e);
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public List<JobStatusDetail> getAllJobStatus(Long tenantId, boolean isSuperAdmin) {

        List<JobStatusMaster> statusMasters;
        Long tenantIdToUse = isSuperAdmin ? 1L : tenantId;
        statusMasters = jobStatusMasterRepository.findAllByTenantIdAndDeletedFalse(tenantIdToUse);

        List<JobStatusDetail> statusDetails = statusMasters.stream()
                .map(statusMaster -> new JobStatusDetail(statusMaster.getUuid(), statusMaster.getName(), statusMaster.getColorCode()))
                .collect(Collectors.toList());

        return statusDetails;
    }

    @Override
    public ResponseEntity<ApiResponse> getFrontOfficeDevices(String id, Long tenantId) throws CodeException {
        return adminClient.getFrontOfficeDevices(id, tenantId);
    }


    List<FormsManagementDTO.Detail> getFormByJobType(String formType, Long tenantId) {
        ResponseEntity<ApiResponse> response = adminClient.getFormByJobTypeId(formType, tenantId);
        ApiResponse body = response.getBody();
        if (body != null) {
            List<FormsManagementDTO.Detail> details = objectMapper.convertValue(
                    body.getData(),
                    new TypeReference<List<FormsManagementDTO.Detail>>() {
                    }
            );
            return details;
        }
        return null;
    }

    @Override
    public FormsResponseDTO getFormsWithTaskId(String taskId, Long tenantId) throws CodeException {
        try {
            Optional<JobTaskMappingTechnician> taskMappingOpt = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(taskId);
            if (taskMappingOpt.isPresent()) {
                Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuidWithJob(taskMappingOpt.get().getJobTaskMappingId());
                if (jobMappingTask.isPresent()) {
                    Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(jobMappingTask.get().getJob().getUuid());
                    if (job.isPresent()) {
                        Optional<JobType> jobTask = jobTypeRepository.findByUuid(job.get().getJobTypeId());
                        if (jobTask.isPresent()) {
                            String jobTypeId = jobTask.get().getUuid();
                            List<FormsManagementDTO.Detail> formsDetails = getFormByJobType(jobTypeId, tenantId);
                            // Replace {{API_URL}} inside content for each form detail
                            String finalApiUrl = baseApiUrl + taskId;
                            formsDetails.forEach(detail -> {
                                if (detail.getContent() != null) {
                                    detail.setContent(
                                            detail.getContent().replace("{{API_URL}}", finalApiUrl)
                                    );
                                }
                            });
                            FormsResponseDTO formsResponseDTO = new FormsResponseDTO();
                            formsResponseDTO.setJobId(jobMappingTask.get().getJob().getUuid());
                            formsResponseDTO.setJobTypeId(jobTypeId);
                            formsResponseDTO.setTaskId(taskId);
                            formsResponseDTO.setFormDetails(formsDetails);
                            return formsResponseDTO;
                        }
                    }
                }

            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<com.octal.fsm.common.ApiResponse> saveTechnicianHtmlForm(HTMLFormDTO.Add add, Long tenantId) throws CodeException {
        if (add.getTaskId() == null) {
            throw new CodeException("Task ID is required", ErrorCode.COMMON);
        }
        Optional<JobTaskMappingTechnician> taskMappingOpt = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(add.getTaskId());
        if (taskMappingOpt.isEmpty()) {
            throw new CodeException("JobTaskMappingTechnician not found for given Task ID", ErrorCode.COMMON);
        }
        JobTaskMappingTechnician technician = taskMappingOpt.get();
        HTMLFormPage htmlFormPage = new HTMLFormPage();
        htmlFormPage.setName(add.getName());
        htmlFormPage.setContent(add.getContent());
        htmlFormPage.setCreatedAt(LocalDateTime.now());
        technician.getHtmlFormPages().add(htmlFormPage);
        JobTaskMappingTechnician save = jobTaskMappingTechnicianRepository.save(technician);
        return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Job created successfully", save, "200", HttpStatus.OK), HttpStatus.OK);
    }

    @Override
    public JobTaskMappingWithHTMLFormDTO getTechnicianHtmlForm(String taskId, Long tenantId) throws CodeException {
        Optional<JobTaskMappingTechnician> taskOpt =
                jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(taskId);

        if (taskOpt.isEmpty()) {
            throw new CodeException("Technician task not found", ErrorCode.COMMON);
        }
        JobTaskMappingTechnician task = taskOpt.get();
        List<HTMLFormDTO.Details> htmlForms = task.getHtmlFormPages().stream()
                .filter(f -> Boolean.TRUE.equals(f.getActive())) // only active ones
                .map(f -> new HTMLFormDTO.Details(
                        f.getUuid(),
                        f.getActive(),
                        f.getName(),
                        f.getContent(),
                        f.getCreatedAt().toString(),
                        f.getUpdatedAt().toString()
                ))
                .collect(Collectors.toList());
        return new JobTaskMappingWithHTMLFormDTO(
                task.getUuid(),
                task.getJobTaskMappingId(),
                task.getTechnicianId(),
                task.getTaskStatus(),
                task.getNote(),
                task.getTechnicianNote(),
                task.getStartDate().toString(),
                task.getEndDate().toString(),
                task.getSignature(),
                task.getCancelReason(),
                task.getDrawingJson(),
                task.getDrawingImage(),
                htmlForms
        );
    }

    @Override
    public void updateJobTaskDetails(String jobId, JobDTO.UpdateJobTaskDetails updateJobTaskDetails, Long tenantId, String userName) throws CodeException {
        Optional<Job> jobOptional = jobRepository.findByUuidAndTenantIdAndDeletedFalse(jobId, tenantId);
        if (jobOptional.isEmpty())
            throw new CodeException("Job not found", ErrorCode.BAD_REQUEST);
        if (updateJobTaskDetails.getAssignedType() == null)
            throw new CodeException("Assigned Type update is not allowed", ErrorCode.BAD_REQUEST);
        if (updateJobTaskDetails.getAssignedType().equals(TaskAssignedType.SYSTEM)) {
            // todo need to perform related task automatically for example BOM generation and quickbooks related stuff.
        } else if (updateJobTaskDetails.getAssignedType().equals(TaskAssignedType.CSR)) {
            if (TextUtils.isEmpty(updateJobTaskDetails.getTaskId()))
                throw new CodeException("Task Id is required to update the task details", ErrorCode.BAD_REQUEST);
            Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(updateJobTaskDetails.getTaskId());
            if (jobMappingTask.isEmpty())
                throw new CodeException("Job Task mapping not found", ErrorCode.BAD_REQUEST);
            if (!updateJobTaskDetails.getDocuments().isEmpty()) {
                try {
                    documentService.uploadMultipleDocumentForCSR(updateJobTaskDetails.getDocuments());
                } catch (Exception e) {
                    throw new CodeException("Error while uploading documents: " + e.getMessage(), ErrorCode.EXCEPTION_OCCUR);
                }
            }
            if (TextUtils.isEmpty(updateJobTaskDetails.getNote()))
                throw new CodeException("Note is required to update the task details", ErrorCode.BAD_REQUEST);
            jobMappingTask.get().setNote(updateJobTaskDetails.getNote());
            jobMappingTaskRepository.save(jobMappingTask.get());
        }
        if (updateJobTaskDetails.getIsDone()) {
            Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(updateJobTaskDetails.getTaskId());
            if (jobMappingTask.isPresent()) {
                JobMappingTask currentTask = jobMappingTask.get();
                int currentSeq = currentTask.getTaskSequence();
                List<JobMappingTask> jobMappingTasks = jobMappingTaskRepository.findByJobOrderByTaskSequenceAsc(jobOptional.get());
                if (!jobMappingTasks.isEmpty()) {
                    JobMappingTask nextTask = null;
                    for (JobMappingTask task : jobMappingTasks) {
                        if (task.getTaskSequence() > currentSeq) {
                            nextTask = task;
                            break;
                        }
                    }
                    if (nextTask != null) {
                        jobOptional.get().setCurrentTaskId(nextTask.getUuid());
                        List<JobStatusMaster> jobStatus = jobStatusMasterRepository.findByName(nextTask.getJobTaskStatus());
                        if (!jobStatus.isEmpty()) {
                            jobOptional.get().setJobStatusMaster(jobStatus.get(0));
                            jobOptional.get().setJobStatus(jobStatus.get(0).getName());
                        }

                    } else {
                        jobOptional.get().setCurrentTaskId(null);
                    }
                    jobRepository.save(jobOptional.get());
                }
            }
        }
    }

    @Override
    public HashMap<String, TechnicianDTO.TaskStats> getTechnicianTaskSummary(List<String> technicianUuids, Long tenantId, boolean isSuperAdmin) {
        if (isSuperAdmin)
            tenantId = 1L;
        List<JobTaskMappingTechnician> taskMappings = jobTaskMappingTechnicianRepository.findByTechnicianIdInAndDeletedFalse(technicianUuids);
        HashMap<String, TechnicianDTO.TaskStats> technicianTaskSummary = new HashMap<>();
        for (String technicianUuid : technicianUuids) {
            long assignedTasks = taskMappings.stream()
                    .filter(mapping -> mapping.getTechnicianId().equals(technicianUuid))
                    .filter(mapping -> !mapping.getTaskStatus().equalsIgnoreCase("COMPLETED")
                            && !mapping.getTaskStatus().equalsIgnoreCase("CANCELLED"))
                    .count();

            long completedTasks = taskMappings.stream()
                    .filter(mapping -> mapping.getTechnicianId().equals(technicianUuid)
                            && mapping.getTaskStatus().equalsIgnoreCase("COMPLETED"))
                    .count();
            long allTasks = taskMappings.stream()
                    .filter(mapping -> mapping.getTechnicianId().equals(technicianUuid))
                    .count();


            TechnicianDTO.TaskStats summary = new TechnicianDTO.TaskStats();
            summary.setAssignedTasks(assignedTasks);
            summary.setCompletedTasks(completedTasks);
            summary.setAllTasks(allTasks);
            technicianTaskSummary.put(technicianUuid, summary);
        }
        return technicianTaskSummary;

    }

    @Override
    public List<TechnicianJobSummaryDTO> getTechnicianAssociationNeeded(Long tenantId, boolean isSuperAdmin) {
        try {
            List<Object[]> stats = jobTaskMappingTechnicianRepository.getTechnicianJobStats();

            Map<String, Long> completedMap = new HashMap<>();
            List<String> availableIds = new ArrayList<>();

            for (Object[] row : stats) {
                String techId = (String) row[0];//techIds
                Long completed = (Long) row[1];//how many task completed
                Long active = (Long) row[2];//active task
                completedMap.put(techId, completed);
                if (active == 0) {
                    availableIds.add(techId);
                }
            }
            // If no available technicians
            if (availableIds.isEmpty()) {
                return Collections.emptyList();
            }
            ApiResponse technicianResponse = technicianClient.getTechByIds(availableIds, tenantId).getBody();
            if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200") && technicianResponse.getData() != null) {
                List<TechnicianDTO.GetDetails> techDetails = objectMapper.convertValue(
                        technicianResponse.getData(),
                        new TypeReference<List<TechnicianDTO.GetDetails>>() {
                        }
                );
                return techDetails.stream()
                        .map(t -> new TechnicianJobSummaryDTO(
                                t.getId(),
                                t.getName(),
                                t.getEmail(),
                                t.getMobileNumber(),
                                t.getProfilePicture(),
                                t.getIsActive(),
                                completedMap.getOrDefault(t.getId(), 0L),
                                t.getJoinedDate()
                        ))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public List<TodayScheduleDTO> getTodayScheduled(Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            LocalDate today = LocalDate.now();
            List<JobTaskMappingTechnician> activeTasksForToday = jobTaskMappingTechnicianRepository.findActiveTasksForToday(today);
            if (activeTasksForToday.isEmpty()) {
                return Collections.emptyList();
            }
            Set<String> technicianIds = activeTasksForToday.stream()
                    .map(JobTaskMappingTechnician::getTechnicianId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<String> jobMappingIds = activeTasksForToday.stream()
                    .map(JobTaskMappingTechnician::getJobTaskMappingId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Job> byUuidIn = jobRepository.findByUuidIn(new ArrayList<>(jobMappingIds));
            Map<String, Job> jobMap = byUuidIn.stream()
                    .collect(Collectors.toMap(Job::getUuid, j -> j));

            ApiResponse technicianResponse = technicianClient.getTechByIds(new ArrayList<>(technicianIds), tenantId).getBody();
            List<TodayScheduleDTO> results = new ArrayList<>();
            if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200") && technicianResponse.getData() != null) {
                List<TechnicianDTO.GetDetails> techDetails = objectMapper.convertValue(
                        technicianResponse.getData(),
                        new TypeReference<List<TechnicianDTO.GetDetails>>() {
                        }
                );
                Map<String, TechnicianDTO.GetDetails> techMap = techDetails.stream()
                        .collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));

                for (JobTaskMappingTechnician task : activeTasksForToday) {
                    TechnicianDTO.GetDetails tech = techMap.get(task.getTechnicianId());
                    Job job = jobMap.get(task.getJobTaskMappingId());
                    if (tech == null || job == null) continue;
                    TodayScheduleDTO dto = new TodayScheduleDTO();
                    dto.setTechnicianName(tech.getName());
                    dto.setServiceLocation(job.getServiceLocation());
                    dto.setStartDate(task.getStartDate());
                    dto.setEndDate(task.getEndDate());
                    results.add(dto);
                }
            }
            return results;
        } catch (Exception e) {
            throw new CodeException("Failed to get today's schedule: " + e.getMessage(), ErrorCode.COMMON);
        }
    }

    @Override
    public PageItem<JobTaskListDTO> getJobTaskList(com.octal.fsm.models.request.PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            Page<JobTaskMappingTechnician> pagedResult = getJobTaskMappingData(listRequest, tenantId, isSuperAdmin);

            Set<String> technicianIds = pagedResult
                    .getContent()
                    .stream()
                    .map(JobTaskMappingTechnician::getTechnicianId)
                    .collect(Collectors.toSet());

            Set<String> jobMappingIds = pagedResult
                    .getContent()
                    .stream()
                    .map(JobTaskMappingTechnician::getJobTaskMappingId)
                    .collect(Collectors.toSet());
            List<Job> jobByUuidIn = jobRepository.findByUuidIn(new ArrayList<>(jobMappingIds));

            Set<String> jobTypeIds = jobByUuidIn.stream()
                    .map(Job::getJobTypeId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<JobType> byUuidAndDeletedFalse = jobTypeRepository.findByUuidAndDeletedFalse(new ArrayList<>(jobTypeIds));

            Map<String, JobType> jobTypeMap = byUuidAndDeletedFalse.stream()
                    .collect(Collectors.toMap(JobType::getUuid, j -> j));

            ApiResponse technicianResponse = technicianClient.getTechByIds(new ArrayList<>(technicianIds), tenantId).getBody();
            List<JobTaskListDTO> results = new ArrayList<>();
            if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200") && technicianResponse.getData() != null) {
                List<TechnicianDTO.GetDetails> techDetails = objectMapper.convertValue(
                        technicianResponse.getData(),
                        new TypeReference<List<TechnicianDTO.GetDetails>>() {
                        }
                );
                Map<String, TechnicianDTO.GetDetails> techMap = techDetails.stream()
                        .collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));

                for (JobTaskMappingTechnician record : pagedResult.getContent()) {

                    TechnicianDTO.GetDetails tech = techMap.get(record.getTechnicianId());
                    Job job = jobByUuidIn.stream()
                            .filter(j -> j.getUuid().equals(record.getJobTaskMappingId()))
                            .findFirst().orElse(null);

                    if (tech == null || job == null)
                        continue;

                    JobType jobType = jobTypeMap.get(job.getJobTypeId());
                    if (jobType == null)
                        continue;

                    JobTaskListDTO dto = new JobTaskListDTO();
                    dto.setTechnicianId(record.getTechnicianId());
                    dto.setTechnicianName(tech.getName());
                    dto.setStartDate(record.getStartDate().toString());
                    dto.setEndDate(record.getEndDate().toString());
                    dto.setTaskStatus(record.getTaskStatus());
                    dto.setTaskName(jobType.getName());
                    dto.setJobId(record.getJobTaskMappingId());

                    results.add(dto);
                }
            }
            if (!TextUtils.isEmpty(listRequest.getSearchText())) {
                String search = listRequest.getSearchText().trim().toLowerCase().replaceAll("\\s+", "");
                results = results.stream()
                        .filter(r -> {
                            if (r.getTechnicianName() == null) return false;
                            String tech = r.getTechnicianName()
                                    .trim()
                                    .toLowerCase()
                                    .replaceAll("\\s+", "");
                            return tech.contains(search);
                        })
                        .collect(Collectors.toList());
            }

            int totalElements = results.size();
            int pageSize = listRequest.getPageSize();
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);

            int page = listRequest.getPageNumber();

            int fromIndex = page * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, totalElements);

            List<JobTaskListDTO> pagedResults = new ArrayList<>();
            if (fromIndex < totalElements) {
                pagedResults = results.subList(fromIndex, toIndex);
            }

            return new PageItem<>(
                    totalPages,
                    totalElements,
                    pagedResults,
                    page,
                    pageSize
            );
        } catch (Exception exception) {
            throw new CodeException("Failed to generate list: " + exception.getMessage(), ErrorCode.COMMON);
        }

    }

    @Override
    public PageItem<JobInvoiceListDTO> getJobCompletedInvoiceList(com.octal.fsm.models.request.PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            Page<Job> pagedResult = getJobMappingData(listRequest, tenantId, isSuperAdmin);
            List<Job> jobs = pagedResult.getContent();

            Set<String> customerIds = jobs.stream()
                    .map(Job::getCustomerId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<CustomerDTO.GetDetails> customerDetails = new ArrayList<>();
            ApiResponse customerResponse = adminClient.getCustomerByIds(
                    new ArrayList<>(customerIds), tenantId, isSuperAdmin).getBody();

            if (customerResponse != null &&
                    "200".equalsIgnoreCase(customerResponse.getStatus()) &&
                    customerResponse.getData() != null) {
                customerDetails = objectMapper.convertValue(
                        customerResponse.getData(),
                        new TypeReference<List<CustomerDTO.GetDetails>>() {
                        }
                );
            }

            Map<String, CustomerDTO.GetDetails> customerMap = customerDetails.stream()
                    .collect(Collectors.toMap(CustomerDTO.GetDetails::getId, c -> c));

            Set<String> jobIds = jobs.stream()
                    .map(Job::getUuid)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<JobInvoice> invoices = jobInvoiceRepository.findByJobIdAndDeletedFalse(new ArrayList<>(jobIds));

            Map<String, JobInvoice> invoiceMap = invoices.stream()
                    .collect(Collectors.toMap(JobInvoice::getJobId, inv -> inv));

            List<JobTaskMappingTechnician> jobTaskMappings =
                    jobTaskMappingTechnicianRepository.findByJobTaskIdAndDeletedFalse(new ArrayList<>(jobIds));

            Set<String> technicianIds = jobTaskMappings.stream()
                    .map(JobTaskMappingTechnician::getTechnicianId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<TechnicianDTO.GetDetails> techDetails = new ArrayList<>();
            ApiResponse techResponse = technicianClient.getTechByIds(new ArrayList<>(technicianIds), tenantId).getBody();
            if (techResponse != null &&
                    "200".equalsIgnoreCase(techResponse.getStatus()) &&
                    techResponse.getData() != null) {

                techDetails = objectMapper.convertValue(
                        techResponse.getData(),
                        new TypeReference<List<TechnicianDTO.GetDetails>>() {
                        }
                );
            }
            Map<String, TechnicianDTO.GetDetails> techMap = techDetails.stream()
                    .collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));

            Set<String> jobTypeIds = jobs.stream()
                    .map(Job::getJobTypeId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<JobType> jobTypes = jobTypeRepository.findByUuidAndDeletedFalse(new ArrayList<>(jobTypeIds));
            Map<String, JobType> jobTypeMap = jobTypes.stream()
                    .collect(Collectors.toMap(JobType::getUuid, jt -> jt));

            List<JobInvoiceListDTO> results = new ArrayList<>();

            for (Job job : jobs) {
                CustomerDTO.GetDetails customer = customerMap.get(job.getCustomerId());
                JobType jobType = jobTypeMap.get(job.getJobTypeId());
                JobTaskMappingTechnician jtMapping = jobTaskMappings.stream()
                        .filter(jt -> jt.getJobTaskMappingId().equals(job.getUuid()))
                        .findFirst().orElse(null);
                TechnicianDTO.GetDetails tech = jtMapping != null ? techMap.get(jtMapping.getTechnicianId()) : null;
                JobInvoice invoice = invoiceMap.get(job.getUuid());
                if (customer == null || jobType == null || tech == null || invoice == null)
                    continue;

                JobInvoiceListDTO dto = new JobInvoiceListDTO();
                dto.setTechnicianId(tech.getId());
                dto.setTechnicianName(tech.getName());
                dto.setCustomerId(customer.getId());
                dto.setCustomerName(customer.getName());
                dto.setJobType(jobType.getName());
                dto.setInvoiceStatus("Generated");
                if (invoice.getPaid()) {
                    dto.setPaymentStatus("Completed");
                } else {
                    dto.setPaymentStatus("Pending");
                }
                dto.setStartDate(job.getJobStartDate() != null ? job.getJobStartDate().toString() : null);
                dto.setEndDate(job.getJobEndDate() != null ? job.getJobEndDate().toString() : null);

                results.add(dto);
            }

            if (!TextUtils.isEmpty(listRequest.getSearchText())) {
                String search = listRequest.getSearchText().trim().toLowerCase().replaceAll("\\s+", "");
                results = results.stream()
                        .filter(r -> r.getCustomerName() != null &&
                                r.getCustomerName().trim().toLowerCase().replaceAll("\\s+", "").contains(search))
                        .collect(Collectors.toList());
            }

            int totalElements = results.size();
            int pageSize = listRequest.getPageSize();
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);

            int page = listRequest.getPageNumber();

            int fromIndex = page * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, totalElements);

            List<JobInvoiceListDTO> pagedResults = new ArrayList<>();
            if (fromIndex < totalElements) {
                pagedResults = results.subList(fromIndex, toIndex);
            }

            return new PageItem<>(
                    totalPages,
                    totalElements,
                    pagedResults,
                    page,
                    pageSize
            );
        } catch (Exception e) {
            throw new CodeException("Failed to generate list: " + e.getMessage(), ErrorCode.COMMON);
        }

    }

    @Override
    public PageItem<TaskManagerDTO> getTaskManagerList(com.octal.fsm.models.request.PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            if (!TextUtils.isEmpty(listRequest.getFrontOfficeId())) {
                return generateDataWithFrontStaffId(listRequest.getFrontOfficeId(), listRequest, tenantId);
            } else {
                return generateDataUsingSpecification(listRequest.getTechnicianId(), listRequest, tenantId);
            }
        } catch (Exception exception) {
            throw new CodeException("Failed to generate list: " + exception.getMessage(), ErrorCode.COMMON);
        }
    }

    @Override
    public TaskManagerDTO getTaskByTaskId(String taskId, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            Optional<JobMappingTask> byUuid = jobMappingTaskRepository.findByUuid(taskId);
            if (byUuid.isPresent()) {
                JobMappingTask entity = byUuid.get();
                TaskManagerDTO dto = new TaskManagerDTO();
                dto.setTaskStatus(entity.getJobTaskStatus());
                dto.setTaskName(entity.getTaskName());
                dto.setTaskId(entity.getTaskId());
                dto.setDate(entity.getCreatedAt().toLocalDate().toString());
                dto.setTime(null);
                dto.setDescription("Description");
                dto.setJobId(entity.getJob().getUuid());
                return dto;
            }
            return null;
        } catch (Exception exception) {
            throw new CodeException("Failed to get Task: " + exception.getMessage(), ErrorCode.COMMON);
        }
    }

    private Page<Job> getJobMappingData(com.octal.fsm.models.request.PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) {
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        prepareJobListSearchFilter(listRequest, builder, tenantId);
        return jobRepository.findAll(builder.build(), pageable);
    }

    private Page<JobTaskMappingTechnician> getJobTaskMappingData(com.octal.fsm.models.request.PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) {
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        GenericSpecificationsBuilder<JobTaskMappingTechnician> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        prepareTaskListSearchFilter(listRequest, builder, tenantId);
        return jobTaskMappingTechnicianRepository.findAll(builder.build(), pageable);
    }

    @Override
    public PageItem<DispatchBoardTechnicianWrapper> getDataForDispatchBoard(com.octal.fsm.models.request.PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) {
        GenericSpecificationsBuilder<JobTaskMappingTechnician> builder = new GenericSpecificationsBuilder<>();
        prepareDispatchSearchFilter(listRequest, builder);

        List<JobTaskMappingTechnician> techMappings = jobTaskMappingTechnicianRepository.findAll(builder.build());

        List<String> uniqueIds = techMappings.stream()
                .map(JobTaskMappingTechnician::getJobTaskMappingId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        GenericSpecificationsBuilder<JobMappingTask> builder1 = new GenericSpecificationsBuilder<>();
        prepareDispatchFilter(listRequest, builder1, uniqueIds, tenantId);
        List<JobMappingTask> all = jobMappingTaskRepository.findAllWithJobAndTags(builder1.build());

        List<TechnicianDTO.GetDetails> techDetailsList = new ArrayList<>();
        PageItem<TechnicianDTO.GetDetails> technicanPageItem = new PageItem<>();
        if (listRequest.getTechnicianId() == null) {
            ApiResponse technicianResponse = technicianClient.getAllTechnician(listRequest, tenantId, false).getBody();
            if (technicianResponse != null && "200".equalsIgnoreCase(technicianResponse.getStatus()) && technicianResponse.getData() != null) {
                PageItem<TechnicianDTO.GetDetails> pageItem = objectMapper.convertValue(
                        technicianResponse.getData(),
                        new TypeReference<PageItem<TechnicianDTO.GetDetails>>() {
                        }
                );
                techDetailsList = pageItem.getItems();
                technicanPageItem = pageItem;
            }
        } else {
            ApiResponse technicianResponse = technicianClient.getTechnicianByUuid(listRequest.getTechnicianId(), tenantId, isSuperAdmin).getBody();
            if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200") && technicianResponse.getData() != null) {
                Gson gson = new Gson();
                TechnicianDTO.GetDetails technicianDetails = gson.fromJson(gson.toJson(technicianResponse.getData()), TechnicianDTO.GetDetails.class);
                techDetailsList.add(technicianDetails);
            }
        }
        List<DispatchBoardTechnicianWrapper> dispatchBoardTechnicianWrappers = buildDispatchBoardData(all, techMappings, techDetailsList, tenantId);
        int totalElements = dispatchBoardTechnicianWrappers.size();
        int pageSize = technicanPageItem.getPageSize();
        int totalPages = technicanPageItem.getTotalPages();

        int page = listRequest.getPageNumber();

        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<DispatchBoardTechnicianWrapper> pagedResults = new ArrayList<>();
        if (fromIndex < totalElements) {
            pagedResults = dispatchBoardTechnicianWrappers.subList(fromIndex, toIndex);
        } else {
            pagedResults = dispatchBoardTechnicianWrappers.subList(0, toIndex);
        }
        return new PageItem<>(
                totalPages,
                totalElements,
                pagedResults,
                page,
                pageSize
        );
    }

    public List<DispatchBoardTechnicianWrapper> buildDispatchBoardData(List<JobMappingTask> tasks, List<JobTaskMappingTechnician> techMappings, List<TechnicianDTO.GetDetails> techDetailsList, Long tenantId) {
        try {
            // 1. Build task map
            final Map<String, JobMappingTask> taskMap = tasks.stream()
                    .collect(Collectors.toMap(JobMappingTask::getUuid, jt -> jt));

            // 2. Collect all TagIds from tasks
            Set<String> allTagIds = tasks.stream()
                    .map(JobMappingTask::getJob)
                    .filter(Objects::nonNull)
                    .flatMap(job -> job.getJobMappingTags().stream())
                    .map(JobMappingTags::getTagId)
                    .collect(Collectors.toSet());

            Set<String> allCustomerIds = tasks.stream()
                    .map(JobMappingTask::getJob)
                    .filter(Objects::nonNull)
                    .map(Job::getCustomerId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            final Map<String, CustomerDTO.GetDetails> customerToNameMap = new HashMap<>();

            ApiResponse customerResponse = adminClient.getCustomerByIds(new ArrayList<>(allCustomerIds), tenantId, false).getBody();

            if (customerResponse != null && "200".equalsIgnoreCase(customerResponse.getStatus()) && customerResponse.getData() != null) {
                List<CustomerDTO.GetDetails> customerDetails = objectMapper.convertValue(
                        customerResponse.getData(),
                        new TypeReference<List<CustomerDTO.GetDetails>>() {
                        }
                );
                customerDetails.forEach(t -> customerToNameMap.put(t.getId(), t));
            }
            // 3. Fetch Tag Master Data
            List<JobTag> jobTags = jobTagRepository.findByUuidIn(new ArrayList<>(allTagIds));

            final Map<String, JobTag> tagIdToNameMap = jobTags.stream()
                    .collect(Collectors.toMap(JobTag::getUuid, jt -> jt));

            List<DispatchBoardDataResponseDTO> collect = techMappings.stream()
                    .map(tech -> {
                        JobMappingTask task = taskMap.get(tech.getJobTaskMappingId());
                        if (task == null) return null;
                        Job job = task.getJob();
                        if (job == null) return null;
                        DispatchBoardDataResponseDTO dto = new DispatchBoardDataResponseDTO();

//                        dto.setTechDetails(techDetails);
//                        dto.setTechnicianName(techDetails != null ? techDetails.getName() : "Unknown");
                        dto.setTechnicianId(tech.getTechnicianId());

                        dto.setJobTaskMappingTechnicianId(tech.getUuid());
                        dto.setTaskMappingId(task.getUuid());

                        CustomerDTO.GetDetails customer = customerToNameMap.get(job.getCustomerId());
                        dto.setCustomerName(customer != null ? customer.getName() : "Unknown");

                        dto.setStartDate(tech.getStartDate() != null ? tech.getStartDate().toString() : null);
                        dto.setEndDate(tech.getEndDate() != null ? tech.getEndDate().toString() : null);
                        dto.setStartTime(tech.getStartTime() != null ? tech.getStartTime().toString() : null);
                        dto.setEndTime(tech.getEndTime() != null ? tech.getEndTime().toString() : null);
                        dto.setJobId(job.getJobId());
                        dto.setCustomerId(job.getCustomerId());
                        dto.setServiceLocation(job.getServiceLocation());
                        dto.setJobTypeId(job.getJobTypeId());
                        dto.setJobStatus(job.getJobStatus());

                        List<JobTagDTO.Detail> tagDetails = job.getJobMappingTags().stream()
                                .map(JobMappingTags::getTagId)
                                .map(tagIdToNameMap::get)
                                .filter(Objects::nonNull)
                                .map(tag -> JobTagDTO.Detail.builder()
                                        .id(tag.getUuid())
                                        .name(tag.getName())
                                        .tagColor(tag.getTagColor())
                                        .build())
                                .collect(Collectors.toList());
                        dto.setJobTag(tagDetails);
                        return dto;

                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<String, List<DispatchBoardDataResponseDTO>> techToTasksMap =
                    collect.stream()
                            .collect(Collectors.groupingBy(DispatchBoardDataResponseDTO::getTechnicianId));
            List<DispatchBoardTechnicianWrapper> result =
                    techDetailsList.stream()
                            .map(tech -> {

                                List<DispatchBoardDataResponseDTO> tasksForTech =
                                        techToTasksMap.getOrDefault(tech.getId(), new ArrayList<>());

                                DispatchBoardTechnicianWrapper wrapper = new DispatchBoardTechnicianWrapper();
                                wrapper.setTechnician(tech);
                                wrapper.setTasks(tasksForTech);
                                return wrapper;
                            })
                            .collect(Collectors.toList());
            return result;

        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public ResponseEntity<com.octal.fsm.common.ApiResponse> getTaskView(String taskId, Long tenantId, boolean isSuperAdmin) {
        try {
            Optional<JobMappingTask> byUuid = jobMappingTaskRepository.findByUuid(taskId);
            if (byUuid.isPresent()) {
                if (byUuid.get().getAssignType().equals(TaskAssignedType.CSR)) {
                    List<Documents> documents = documentsRepository.findByAttachTypeId(taskId);
                    List<DocumentDTO.Add> documentDTO = documents
                            .stream()
                            .map(this::convertDocumentToDto)
                            .collect(Collectors.toList());

                    JobMappingTask entity = byUuid.get();
                    JobMappingTaskDTO dto = new JobMappingTaskDTO();
                    dto.setUuid(entity.getUuid());
                    dto.setTaskId(entity.getTaskId());
                    dto.setTaskShowId(entity.getTaskShowId());
                    dto.setTaskName(entity.getTaskName());
                    dto.setDocumentTypeId(entity.getDocumentTypeId());
                    dto.setTaskSequence(entity.getTaskSequence());
                    dto.setJobTaskStatus(entity.getJobTaskStatus());
                    dto.setDocuments(documentDTO);
                    dto.setAssignType(entity.getAssignType());
                    dto.setNote(entity.getNote());
                    return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Task fetched successfully", dto, "200", HttpStatus.OK), HttpStatus.OK);
                } else if (byUuid.get().getAssignType().equals(TaskAssignedType.TECHNICIAN)) {
                    Optional<JobTaskMappingTechnician> byJobTaskMappingId = jobTaskMappingTechnicianRepository.findByJobTaskMappingId(byUuid.get().getUuid());
                    if (byJobTaskMappingId.isPresent()) {
                        List<Documents> documents = documentsRepository.findByAttachTypeId(taskId);
                        List<DocumentDTO.Add> documentDTO = documents
                                .stream()
                                .map(this::convertDocumentToDto)
                                .collect(Collectors.toList());
                        JobTaskMappingTechnician entity = byJobTaskMappingId.get();
                        JobMappingTaskDTO dto = new JobMappingTaskDTO();
                        dto.setUuid(entity.getUuid());
                        dto.setTaskId(byUuid.get().getUuid());
                        dto.setNote(entity.getNote());
                        dto.setHtmlFormPages(entity.getHtmlFormPages());
                        dto.setDocumentTypeId(entity.getDocuments());
                        dto.setJobTaskStatus(entity.getTaskStatus());
                        dto.setDocuments(documentDTO);
                        dto.setAssignType(byUuid.get().getAssignType());
                        dto.setNote(entity.getNote());
                        return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Task fetched successfully", dto, "200", HttpStatus.OK), HttpStatus.OK);
                    }
                } else {
                    return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, "Task Not Available", null, "500", HttpStatus.OK), HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, "Task Not Available", null, "500", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, exception.getMessage(), null, "500", HttpStatus.OK), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<com.octal.fsm.common.ApiResponse> getNotesByJobId(String jobId, Long tenantId, boolean isSuperAdmin) {
        try {
            List<JobNotes> jobNotes = jobNotesRepository.findByJobId(jobId);
            JobFullNotesDTO response = new JobFullNotesDTO();
            response.setJobId(jobId);
            response.setJobNotes(
                    jobNotes.stream()
                            .map(n -> {
                                JobFullNotesDTO.JobNotesDTO dto = new JobFullNotesDTO.JobNotesDTO();
                                dto.setNote(n.getNotes());
                                dto.setCreatedAt(n.getCreatedAt());
                                return dto;
                            }).collect(Collectors.toList())
            );
            List<JobMappingTask> tasks = jobMappingTaskRepository.findByJob_Uuid(jobId);
            List<String> taskIds = tasks.stream().map(JobMappingTask::getUuid).collect(Collectors.toList());

            List<JobTaskMappingTechnician> technicians = jobTaskMappingTechnicianRepository.findByJobTaskIdAndDeletedFalse(taskIds);

            List<JobFullNotesDTO.TaskNotesDTO> taskNotesList = new ArrayList<>();

            for (JobMappingTask task : tasks) {

                JobFullNotesDTO.TaskNotesDTO taskDto = new JobFullNotesDTO.TaskNotesDTO();
                taskDto.setTaskId(task.getUuid());
                taskDto.setTaskName(task.getTaskName());
                taskDto.setTaskNote(task.getNote());
                taskDto.setCreatedAt(task.getCreatedAt());
                JobTaskMappingTechnician tech =
                        technicians.stream()
                                .filter(x -> x.getJobTaskMappingId().equals(task.getUuid()))
                                .findFirst()
                                .orElse(null);

                if (tech != null) {

                    JobFullNotesDTO.TechnicianNotesDTO techDto = new JobFullNotesDTO.TechnicianNotesDTO();
                    techDto.setTechnicianId(tech.getTechnicianId());
                    techDto.setTaskNote(tech.getNote());
                    techDto.setCreatedAt(tech.getCreatedAt());
                    taskDto.setTechnicians(techDto);
                }
                taskNotesList.add(taskDto);
            }
            response.setTaskNotes(taskNotesList);
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Notes fetched successfully", response, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, exception.getMessage(), null, "500", HttpStatus.OK), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<com.octal.fsm.common.ApiResponse> getFormsByJobId(String jobId, Long tenantId, boolean isSuperAdmin) {
        try {
            Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(jobId);
            if (job.isPresent()) {
                List<FormsManagementDTO.Detail> formsDetails = getFormByJobType(job.get().getJobTypeId(), tenantId);
                String finalApiUrl = baseApiUrl + jobId;
                formsDetails.forEach(detail -> {
                    if (detail.getContent() != null) {
                        detail.setContent(
                                detail.getContent().replace("{{API_URL}}", finalApiUrl)
                        );
                    }
                });
                FormsResponseDTO formsResponseDTO = new FormsResponseDTO();
                formsResponseDTO.setJobId(jobId);
                formsResponseDTO.setJobTypeId(job.get().getJobTypeId());
                formsResponseDTO.setFormDetails(formsDetails);
                return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Forms fetched successfully", formsResponseDTO, "200", HttpStatus.OK), HttpStatus.OK);
            }
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, "Forms Not Available", null, "500", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, exception.getMessage(), null, "500", HttpStatus.OK), HttpStatus.OK);
        }
    }

    private DocumentDTO.Add convertDocumentToDto(Documents doc) {
        DocumentDTO.Add dto = new DocumentDTO.Add();
        dto.setFileName(doc.getFileName());
        dto.setDocumentUrl(doc.getDocumentUrl());
        dto.setThumbnail(doc.getThumbnail());
        dto.setDocumentTypeId(doc.getDocumentTypeId());
        dto.setFileType(doc.getFileType());
        dto.setAttachType(doc.getAttachType());
        dto.setAttachTypeId(doc.getAttachTypeId());
        dto.setUploadByUserName(doc.getUploadedByUserName());
        dto.setUploadedBType(doc.getUploadedByType());
        dto.setUploadedBTypeId(doc.getUploadedByTypeId());
        return dto;
    }


    private void prepareDispatchSearchFilter(com.octal.fsm.models.request.PageRequest.List listRequest, GenericSpecificationsBuilder<JobTaskMappingTechnician> builder) {
        builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("deleted", false));

        LocalDate start = listRequest.getStartDate(); // user start date
        LocalDate end = listRequest.getEndDate();     // user end date

        if (start != null && end != null) {

            // Overlap condition:
            // task.startDate <= userEnd   AND   task.endDate >= userStart

            builder.with(jobTaskMappingTechnicianSpecificationFactory
                    .isLessThanOrEquals("startDate", end));   // task.startDate <= userEnd

            builder.with(jobTaskMappingTechnicianSpecificationFactory
                    .isGreaterThanOrEquals("endDate", start)); // task.endDate >= userStart

        } else if (start != null) {


            // selectedDate >= task.startDate
            builder.with(jobTaskMappingTechnicianSpecificationFactory
                    .isLessThanOrEquals("startDate", start));

            // selectedDate <= task.endDate
            builder.with(jobTaskMappingTechnicianSpecificationFactory
                    .isGreaterThanOrEquals("endDate", start));

        } else if (end != null) {

            // user selected only end → get tasks starting on or before the end
            builder.with(jobTaskMappingTechnicianSpecificationFactory
                    .isLessThanOrEquals("startDate", end));
        }

//        if (start != null && end != null) {
//            builder.with(jobTaskMappingTechnicianSpecificationFactory.isGreaterThanOrEquals("startDate", start));
//            builder.with(jobTaskMappingTechnicianSpecificationFactory.isLessThanOrEquals("endDate", end));
//        } else if (start != null) {
//            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("startDate", start));
//        } else if (end != null) {
//            builder.with(jobTaskMappingTechnicianSpecificationFactory.isLessThanOrEquals("endDate", end));
//        }
    }

    private void prepareTaskListSearchFilter(com.octal.fsm.models.request.PageRequest.List listRequest, GenericSpecificationsBuilder<JobTaskMappingTechnician> builder, Long tenantId) {
        builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("deleted", false));
//        if (!TextUtils.isEmpty(tenantId)) {
//            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("tenantId", tenantId));
//        }
        if (listRequest.getIsActive() != null) {
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (!TextUtils.isEmpty(listRequest.getJobStatus())) {
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("taskStatus", listRequest.getJobStatus()));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isGreaterThanOrEquals("startDate", listRequest.getStartDate()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isLessThanOrEquals("endDate", listRequest.getEndDate()));
        }
    }

    private void prepareJobListSearchFilter(com.octal.fsm.models.request.PageRequest.List listRequest, GenericSpecificationsBuilder<Job> builder, Long tenantId) {
        builder.with(jobSpecificationFactory.isEqual("deleted", false));
        if (!TextUtils.isEmpty(tenantId)) {
            builder.with(jobSpecificationFactory.isEqual("tenantId", tenantId));
        }

        if (listRequest.getIsActive() != null) {
            builder.with(jobSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }
        if (!TextUtils.isEmpty(listRequest.getJobStatus())) {
            builder.with(jobSpecificationFactory.isEqual("jobStatus", listRequest.getJobStatus()));
        }
        if (listRequest.getStartDate() != null) {
            builder.with(jobSpecificationFactory.isGreaterThanOrEquals("jobStartDate", listRequest.getStartDate()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(jobSpecificationFactory.isLessThanOrEquals("jobEndDate", listRequest.getEndDate()));
        }
    }

    public PageItem<TaskManagerDTO> generateDataUsingSpecification(String technicianId, com.octal.fsm.models.request.PageRequest.List listReq, Long tenantId) {
        try {
            List<JobTaskMappingTechnician> jobTaskMappingTechnicians = jobTaskMappingTechnicianRepository.findByTechnicianId(technicianId);

            Set<String> mappingIds = jobTaskMappingTechnicians.stream().map(JobTaskMappingTechnician::getJobTaskMappingId).collect(Collectors.toSet());
            String trimmedText = listReq.getSearchText().trim();
            listReq.setSearchText(trimmedText);
            GenericSpecificationsBuilder<JobMappingTask> builder = new GenericSpecificationsBuilder<>();
            Pageable pageable = null;
            if (Boolean.TRUE.equals(listReq.getAsc())) {
                pageable = org.springframework.data.domain.PageRequest.of(listReq.getPageNumber(), listReq.getPageSize(), Sort.by(listReq.getShortingField()).ascending());
            } else {
                pageable = org.springframework.data.domain.PageRequest.of(listReq.getPageNumber(), listReq.getPageSize(), Sort.by(listReq.getShortingField()).descending());
            }
            Page<JobMappingTask> pageData = new PageImpl<>(Collections.emptyList(), pageable, 0);
            if (!TextUtils.isEmpty(technicianId) && !jobTaskMappingTechnicians.isEmpty()) {
                prepareTechnicianTaskFilters(listReq, builder, technicianId, tenantId, new ArrayList<>(mappingIds));
                pageData = jobMappingTaskRepository.findAll(builder.build(), pageable);
            }
            if (TextUtils.isEmpty(technicianId)) {
                prepareTechnicianTaskFilters(listReq, builder, technicianId, tenantId, new ArrayList<>(mappingIds));
                pageData = jobMappingTaskRepository.findAll(builder.build(), pageable);
            }


            List<TaskManagerDTO> responseList = new ArrayList<>();
            for (JobMappingTask department : pageData.getContent()) {
                TaskManagerDTO dto = new TaskManagerDTO();
                dto.setJobId(department.getJob().getUuid());
                dto.setTaskId(department.getUuid());
                dto.setTaskName(department.getTaskName());
                dto.setTaskStatus(department.getJobTaskStatus());
                dto.setDate(String.valueOf(department.getCreatedAt()));
                dto.setDescription("Description");
                dto.setTime(null);
                responseList.add(dto);
            }
            return new PageItem<>(pageData.getTotalPages(), pageData.getTotalElements(), responseList, listReq.getPageNumber(),
                    listReq.getPageSize());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

    private void prepareTechnicianTaskFilters(com.octal.fsm.models.request.PageRequest.List listReq, GenericSpecificationsBuilder<JobMappingTask> builder,
                                              String frontOfficeId, Long tenantId, List<String> jobTaskMappingIds) {

        builder.with(jobMappingTaskSpecificationFactory.joinEqualsLong("job", "tenantId", tenantId));

        builder.with(
                jobMappingTaskSpecificationFactory.isEqual("assignType", TaskAssignedType.TECHNICIAN)
        );

        builder.with(jobMappingTaskSpecificationFactory.isEqual("deleted", false));

        if (jobTaskMappingIds != null && !jobTaskMappingIds.isEmpty()) {
            builder.with(jobMappingTaskSpecificationFactory.in("uuid", jobTaskMappingIds));
        }

        // Filter: jobTaskStatus
        if (!TextUtils.isEmpty(listReq.getTaskStatus())) {
            builder.with(jobMappingTaskSpecificationFactory.isEqual("jobTaskStatus", listReq.getTaskStatus()));
        }

        // Search filter: taskName LIKE OR taskId LIKE
        if (!TextUtils.isEmpty(listReq.getSearchText())) {
            Specification<JobMappingTask> orCondition =
                    jobMappingTaskSpecificationFactory.like("taskName", listReq.getSearchText())
                            .or(jobMappingTaskSpecificationFactory.like("taskId", listReq.getSearchText()));
            builder.with(orCondition);
        }

        if (listReq.getStartDate() != null) {
            builder.with(jobMappingTaskSpecificationFactory.isGreaterThanOrEquals(
                    "createdAt",
                    listReq.getStartDate().atStartOfDay()
            ));
        }

        if (listReq.getEndDate() != null) {
            builder.with(jobMappingTaskSpecificationFactory.isLessThanOrEquals(
                    "createdAt",
                    listReq.getEndDate().atTime(23, 59, 59)
            ));
        }
    }

    public PageItem<TaskManagerDTO> generateDataWithFrontStaffId(String frontOfficeId, com.octal.fsm.models.request.PageRequest.List listReq, Long tenantId) throws CodeException {
        try {
            String trimmedText = listReq.getSearchText().trim();
            listReq.setSearchText(trimmedText);
            GenericSpecificationsBuilder<JobMappingTask> builder = new GenericSpecificationsBuilder<>();
            Pageable pageable1 = null;
            if (Boolean.TRUE.equals(listReq.getAsc())) {
                pageable1 = org.springframework.data.domain.PageRequest.of(listReq.getPageNumber(), listReq.getPageSize(), Sort.by(listReq.getShortingField()).ascending());
            } else {
                pageable1 = org.springframework.data.domain.PageRequest.of(listReq.getPageNumber(), listReq.getPageSize(), Sort.by(listReq.getShortingField()).descending());
            }
            prepareTaskFilter(listReq, builder, frontOfficeId, tenantId);

            Page<JobMappingTask> page = jobMappingTaskRepository.findAll(builder.build(), pageable1);

            List<TaskManagerDTO> responseList = new ArrayList<>();
            for (JobMappingTask department : page.getContent()) {
                TaskManagerDTO dto = new TaskManagerDTO();
                dto.setJobId(department.getJob().getUuid());
                dto.setTaskId(department.getUuid());
                dto.setTaskName(department.getTaskName());
                dto.setTaskStatus(department.getJobTaskStatus());
                dto.setDate(String.valueOf(department.getCreatedAt()));
                dto.setDescription("Description");
                dto.setTime(null);
                responseList.add(dto);
            }
            return new PageItem<>(page.getTotalPages(), page.getTotalElements(), responseList, listReq.getPageNumber(),
                    listReq.getPageSize());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareTaskFilter(com.octal.fsm.models.request.PageRequest.List listReq, GenericSpecificationsBuilder<JobMappingTask> builder, String frontOfficeId,
                                   Long tenantId) {
        builder.with(jobMappingTaskSpecificationFactory.joinEqualsLong("job", "tenantId", tenantId));
        builder.with(jobMappingTaskSpecificationFactory.joinEquals("job", "frontOfficeId", frontOfficeId));

        builder.with(jobMappingTaskSpecificationFactory.isEqual("assignType", TaskAssignedType.CSR));//for CSR

        builder.with(jobMappingTaskSpecificationFactory.isEqual("deleted", false));

        if (!TextUtils.isEmpty(listReq.getJobStatus())) {
            builder.with(jobMappingTaskSpecificationFactory.isEqual("jobTaskStatus", listReq.getTaskStatus()));
        }

        if (org.apache.commons.lang.StringUtils.isNotBlank(listReq.getSearchText())) {
            builder.with(jobMappingTaskSpecificationFactory.like("taskName", listReq.getSearchText()).
                    or(jobMappingTaskSpecificationFactory.like("taskId", listReq.getSearchText())));
        }

        if (listReq.getStartDate() != null) {
            builder.with(jobMappingTaskSpecificationFactory.isGreaterThanOrEquals(
                    "createdAt", listReq.getStartDate().atStartOfDay()
            ));
        }

        if (listReq.getEndDate() != null) {
            builder.with(jobMappingTaskSpecificationFactory.isLessThanOrEquals(
                    "createdAt", listReq.getEndDate().atTime(23, 59, 59)
            ));
        }
    }

    private void prepareDispatchFilter(com.octal.fsm.models.request.PageRequest.List listReq, GenericSpecificationsBuilder<JobMappingTask> builder, List<String> ids, Long tenantId) {
        builder.with(jobMappingTaskSpecificationFactory.joinEqualsLong("job", "tenantId", tenantId));

        builder.with(jobMappingTaskSpecificationFactory.isEqual("deleted", false));

        if (ids != null && !ids.isEmpty()) {
            builder.with(jobMappingTaskSpecificationFactory.in("uuid", ids));
        }
        if (!TextUtils.isEmpty(listReq.getJobTypeId())) {
            builder.with(jobMappingTaskSpecificationFactory.joinEquals("job", "jobTypeId", listReq.getJobTypeId()));
        }
        if (!TextUtils.isEmpty(listReq.getLocationName())) {
            builder.with(jobMappingTaskSpecificationFactory.joinEquals("job", "serviceLocation", listReq.getLocationName()));
        }

        if (!TextUtils.isEmpty(listReq.getJobStatus())) {
            builder.with(jobMappingTaskSpecificationFactory.joinEquals("job", "jobStatus", listReq.getJobStatus()));
        }

    }


}