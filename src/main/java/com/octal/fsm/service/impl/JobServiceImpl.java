package com.octal.fsm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.octal.fsm.clients.*;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.clients.NotificationClient;
import com.octal.fsm.clients.QuickBookClientService;
import com.octal.fsm.clients.TechnicianClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.dto.JobDTO.JobStatusDetail;
import com.octal.fsm.dto.enums.JobUpdateType;
import com.octal.fsm.entities.*;
import com.octal.fsm.entities.enums.TaskAssignedType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.helper.CodeGenerator;
import com.octal.fsm.listener.events.SendMailAndPushEvent;
import com.octal.fsm.listener.events.SendMailToTechnicianEvent;
import com.octal.fsm.repositories.*;
import com.octal.fsm.service.AdminClientService;
import com.octal.fsm.service.DocumentService;
import com.octal.fsm.service.GeneralSettingService;
import com.octal.fsm.service.JobService;
import com.octal.fsm.service.TechnicianClientService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.transformer.JobTransformer;
import com.octal.fsm.utils.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private GeneralSettingService generalSettingService;

    @Autowired
    private TechnicianClientService technicianClientService;
    @Autowired
    private AdminClientService adminClientService;
    @Autowired
    private CodeGenerator codeGenerator;

    @Autowired
    private QuickBookClientService quickBookClientService;
    @Autowired
    private InventoryRequestRepository inventoryRequestRepository;

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
    public ResponseEntity<com.octal.fsm.common.ApiResponse> updateJob(JobDTO.Add addJobDTO, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            addJobDTO.setTenantId(!isSuperAdmin ? tenantId : 1L);
            if (addJobDTO.getJobUuiId() == null)
                throw new CodeException("Job Uuid required", ErrorCode.COMMON);
            if (TextUtils.isEmpty(addJobDTO.getServiceLocation()))
                throw new CodeException("Service Location is required", ErrorCode.COMMON);
            if (addJobDTO.getServiceLocationLat() == null)
                throw new CodeException("Service Location Latitude is required", ErrorCode.COMMON);
            if (addJobDTO.getServiceLocationLng() == null)
                throw new CodeException("Service Location Longitude is required", ErrorCode.COMMON);
            if (TextUtils.isEmpty(addJobDTO.getJobDescription()))
                throw new CodeException("Job Description is required", ErrorCode.COMMON);
            if (TextUtils.isEmpty(addJobDTO.getJobStartDate()))
                throw new CodeException("Job Start Date is required", ErrorCode.COMMON);
//            if (TextUtils.isEmpty(addJobDTO.getJobEndDate()))
//                throw new CodeException("Job End Date is required", ErrorCode.COMMON);
            if (addJobDTO.getJobTags() == null || addJobDTO.getJobTags().isEmpty())
                throw new CodeException("At least one Job Tag is required", ErrorCode.COMMON);
            return jobTransformer.updateJob(addJobDTO, tenantId, isSuperAdmin);
        } catch (Exception e) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, e.getMessage(), null, "101", HttpStatus.OK), HttpStatus.OK);
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
//        CreateInvoiceDTO invoiceResponse = null;
        if (!TextUtils.isEmpty(createUpFrontInvoice.getDueDate())) {
            invoiceRequest.setDueDate(createUpFrontInvoice.getDueDate());
        }
        if (!TextUtils.isEmpty(createUpFrontInvoice.getNote())) {
            invoiceRequest.setPrivateNote(createUpFrontInvoice.getNote());
        }
        try {
            // Queue Invoice Creation
            InvoiceRestDTO.Add invoiceDto = new InvoiceRestDTO.Add();
            invoiceDto.setCustomerFullName("Unknown");
            try {
                ApiResponse customerResponse = adminClient.getCustomerById(job.get().getCustomerId()).getBody();
                if (customerResponse != null && customerResponse.getStatus() != null && customerResponse.getStatus().equalsIgnoreCase("200") && customerResponse.getData() != null) {
                    Gson gson = new Gson();
                    CustomerDTO.GetDetails customerDetails = gson.fromJson(gson.toJson(customerResponse.getData()), CustomerDTO.GetDetails.class);
                    invoiceDto.setCustomerFullName(customerDetails.getName());
                    if(customerDetails.getQuickBookUserId() != null){
                        invoiceDto.setCustomerListId(customerDetails.getQuickBookUserId());
                    }else{
                        invoiceDto.setCustomerListId(customerDetails.getId());
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching customer details: {}", e.getMessage());
            }
            invoiceDto.setSyncStatus("QUEUE");
            invoiceDto.setAmount(BigDecimal.valueOf(createUpFrontInvoice.getAmount()).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
            JobInvoice jobInvoice = new JobInvoice();
            Gson gson = new Gson();
            jobInvoice.setJobId(createUpFrontInvoice.getJobId());
            invoiceDto.setInvoiceId(jobInvoice.getUuid());
            try{
                ApiResponse invoiceQueue = quickBookClientService.createInvoiceQueue(invoiceDto, 1L).getBody();
                if (invoiceQueue == null) {
                    throw new CodeException("Quick Book service returned empty response", ErrorCode.COMMON);
                }
                if ("200".equalsIgnoreCase(invoiceQueue.getStatus()) && invoiceQueue.getData() != null) {
                    String invoiceQueueId = (String) invoiceQueue.getData();
                    jobInvoice.setInvoiceId(invoiceQueueId);
                    jobInvoice.setResponseDTO(gson.toJson(invoiceQueue));
                }
            }
            catch (Exception e) {
                throw new CodeException("Invoice creation failed: " +e.getMessage(), ErrorCode.COMMON);
            }
//            invoiceResponse = quickBooksCustomerService.createInvoice(invoiceRequest);
            //Send Mail
//            JsonNode sendMailResponse = quickBooksCustomerService.sendInvoice(invoiceResponse.getInvoice().getId(), createUpFrontInvoice.getEmail());
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
            jobInvoice.setNote(createUpFrontInvoice.getNote());
            jobInvoice.setRequestDTO(gson.toJson(invoiceRequest));
            jobInvoice.setPaid(false);
            jobInvoice.setInvoiceType(createUpFrontInvoice.getInvoiceType());
            jobInvoiceRepository.save(jobInvoice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public PageItem<JobDTO.InvoiceListResponse> getAllJobInvoices(int page, int size, String sortBy, Boolean order, String jobId, String loggedInUserEmail,Long tenantId) throws CodeException {
        Boolean jobExist = jobRepository.existsByUuidAndDeletedFalse(jobId);
        if (!jobExist)
            throw new CodeException("Job Not Found", ErrorCode.COMMON);
        GenericSpecificationsBuilder<JobInvoice> builder = new GenericSpecificationsBuilder<>();
        size = generalSettingService.getPageSize(tenantId);
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
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
        for (JobInvoice jobInvoice : pagedResult.getContent()) {
            JobDTO.InvoiceListResponse dto = new JobDTO.InvoiceListResponse();
            dto.setId(jobInvoice.getUuid());
            dto.setInvoiceId(jobInvoice.getInvoiceId());
            dto.setInvoiceType(jobInvoice.getInvoiceType());
            dto.setAmount(jobInvoice.getAmount());
            dto.setSendOnEmail(jobInvoice.getSendOnEmail());
            if(jobInvoice.getDueDate() != null){
                dto.setDueDate(dateTimeFormatter != null ? jobInvoice.getDueDate().format(dateTimeFormatter) : jobInvoice.getDueDate().toString());
            }
            dto.setNote(jobInvoice.getNote());
            if(jobInvoice.getCreatedAt() != null){
                dto.setCreatedAt(dateTimeFormatter != null ? jobInvoice.getCreatedAt().format(dateTimeFormatter) : jobInvoice.getCreatedAt().toString());
            }
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
    public PageItem<JobDTO.JobListResponse> getAllJobs(String txt, int page, int size, String sortBy, Boolean order, String jobType, String jobStatus, String jobTag, Double serviceLocationLat, Double serviceLocationLng, String customerType, String fromStartDate, String toStartDate, String location, String loggedInUserEmail, Long tenantId, Boolean isSuperAdmin, String frontOfficeId, List<String> customerIds) throws CodeException {

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

        Specification<Job> searchSpec = null;

        // jobId LIKE txt
        if (!TextUtils.isEmpty(txt)) {
            searchSpec = jobSpecificationFactory.like("jobId", txt);
        }

        // customerId IN customerIds
        if (customerIds != null && !customerIds.isEmpty()) {
            Specification<Job> customerSpec = jobSpecificationFactory.in("customerId", customerIds);

            // combine with OR
            if (searchSpec == null) {
                searchSpec = customerSpec;
            } else {
                searchSpec = searchSpec.or(customerSpec);   // <-- key line
            }
        }
        // now add the combined OR spec into builder (AND with other filters)
        if (searchSpec != null) {
            builder.with(searchSpec);
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
        DateTimeFormatter dateFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
        for (Job job : pagedResult.getContent()) {
            JobDTO.JobListResponse dto = new JobDTO.JobListResponse();
            dto.setId(job.getUuid());
            dto.setJobId(job.getJobId());
            dto.setServiceLocation(job.getServiceLocation());
            Optional<JobType> jobTypeOpt = jobTypeRepository.findByUuid(job.getJobTypeId());
            jobTypeOpt.ifPresent(type -> dto.setJobType(type.getName()));
            if(job.getJobStartDate() != null){
                dto.setJobStartDate(dateFormatter != null ? job.getJobStartDate().format(dateFormatter) : job.getJobStartDate().toString());
            }
            if(job.getJobEndDate() != null){
                dto.setJobEndDate(dateFormatter != null ? job.getJobEndDate().format(dateFormatter) : job.getJobEndDate().toString());
            }
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
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
        response.setLeadReceivedDate(job.getLeadReceivedDate() != null ? job.getLeadReceivedDate().format(dateTimeFormatter) : null);
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
        if(job.getJobStartDate() != null){
            response.setJobStartDate(dateTimeFormatter != null ? job.getJobStartDate().format(dateTimeFormatter) : job.getJobStartDate().toString());
        }
        if(job.getJobEndDate() != null){
            response.setJobEndDate(dateTimeFormatter != null ? job.getJobEndDate().format(dateTimeFormatter) : job.getJobEndDate().toString());
        }
        response.setServiceLocation(job.getServiceLocation());
        response.setServiceLocationLat(job.getServiceLocationLat());
        response.setServiceLocationLng(job.getServiceLocationLng());
        response.setJobStatus(job.getJobStatus());
        return response;
    }

    @Override
    public PageItem<JobDTO.JobTaskListResponse> getJobTask(int page, int size, String sortBy, Boolean order, String jobId,Long tenantId,Boolean isSuperAdmin, String loggedInUserEmail) throws CodeException {
        if (isSuperAdmin)
            tenantId = 1L;
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
                dto.setNote(jobMappingTask.getNote());
                dto.setDocuments(documentService.getJobDocuments(jobMappingTask.getUuid(),tenantId,false));
                Optional<JobTaskMappingTechnician> jobTaskMappingTechnician = jobTaskMappingTechnicianRepository.findByJobTaskMappingId(jobMappingTask.getUuid());
                if (jobTaskMappingTechnician.isPresent()) {
                    dto.setCreatedAt(jobTaskMappingTechnician.get().getCreatedAt() != null ? jobTaskMappingTechnician.get().getCreatedAt().toString() : null);
                    dto.setTaskStatus(jobTaskMappingTechnician.get().getTaskStatus());
                    dto.setStartDate(jobTaskMappingTechnician.get().getStartDate() != null ? jobTaskMappingTechnician.get().getStartDate().toString() : null);
                    dto.setEndDate(jobTaskMappingTechnician.get().getEndDate() != null ? jobTaskMappingTechnician.get().getEndDate().toString() : null);
                    dto.setStartTime(jobTaskMappingTechnician.get().getStartTime() != null ? jobTaskMappingTechnician.get().getStartTime().toString() : null);
                    dto.setEndTime(jobTaskMappingTechnician.get().getEndTime() != null ? jobTaskMappingTechnician.get().getEndTime().toString() : null);
                    TechnicianDTO.GetDetails technicianDetails = technicianClientService.getTechnicianById(jobTaskMappingTechnician.get().getTechnicianId(), loggedInUserEmail);
                    if(technicianDetails != null){
                        dto.setTechnicianName(technicianDetails.getName());
                        dto.setTechnicianId(technicianDetails.getId());
                    }
                } else {
                    if(jobMappingTask.getJobTaskStatus().equalsIgnoreCase("COMPLETED")){
                        dto.setTaskStatus(jobMappingTask.getJobTaskStatus());
                    }else{
                        dto.setTaskStatus("NOT ASSIGNED");
                    }
                }
                responseList.add(dto);
            }
            responseList.sort(Comparator.comparing(
                            (JobDTO.JobTaskListResponse jobDto) -> "COMPLETED".equalsIgnoreCase(jobDto.getTaskStatus()))
                    .thenComparing(JobDTO.JobTaskListResponse::getSequenceNumber));
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
        TechnicianDTO.GetDetails technicianDetails = technicianClientService.getTechnicianById(assignJobToTechnician.getTechnicianId(), loggedInUserEmail);
        if (technicianDetails != null) {
            JobTaskMappingTechnician jobTaskMappingTechnician = new JobTaskMappingTechnician();
            Optional<JobTaskMappingTechnician> jobTaskMappingToTechnician = jobTaskMappingTechnicianRepository.findByJobTaskMappingId(assignJobToTechnician.getJobTaskMappingId());
            if (jobTaskMappingToTechnician.isPresent()) {
                boolean checkIfTaskAssignedToTechnician = checkIfTaskAssignedToTechnician(assignJobToTechnician);
                if (!checkIfTaskAssignedToTechnician && !jobTaskMappingToTechnician.get().getTaskStatus().equalsIgnoreCase("cancelled")) {
                    throw new CodeException("Technician is already assigned to another task during the selected time period.", ErrorCode.COMMON);
                }
                //Todo need to create log for all assignment and reassignment of technician
                jobTaskMappingToTechnician.get().setTechnicianId(assignJobToTechnician.getTechnicianId());
                jobTaskMappingToTechnician.get().setNote(assignJobToTechnician.getNote());
                jobTaskMappingToTechnician.get().setTaskStatus("ASSIGNED");
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
//                if (assignJobToTechnician.getDocuments() != null && !assignJobToTechnician.getDocuments().isEmpty()) {
//                    Gson gson = new Gson();
//                    jobTaskMappingToTechnician.get().setDocuments(gson.toJson(assignJobToTechnician.getDocuments()));
//                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
                JobTaskMappingTechnician save = jobTaskMappingTechnicianRepository.save(jobTaskMappingToTechnician.get());
                try {
                    assignJobToTechnician.setTaskName(jobMappingTask.get().getTaskName());
                    assignJobToTechnician.setTaskShowId(jobMappingTask.get().getTaskShowId());
                    boolean notificationEnabled = generalSettingService.isNotificationEnabled(tenantId);
                    if(notificationEnabled){
                        applicationEventPublisher.publishEvent(new SendMailToTechnicianEvent(assignJobToTechnician, save, loggedInUserEmail, tenantId, isSuperAdmin));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                boolean checkIfTaskAssignedToTechnician = checkIfTaskAssignedToTechnician(assignJobToTechnician);
                if (!checkIfTaskAssignedToTechnician) {
                    throw new CodeException("Technician is already assigned to another task during the selected time period.", ErrorCode.COMMON);
                }

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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                try{
                    if (assignJobToTechnician.getStartDateTime() != null) {
                        LocalDateTime ldt = LocalDateTime.parse(assignJobToTechnician.getStartDateTime(), formatter);
                        Time sqlTime = Time.valueOf(ldt.toLocalTime());
                        jobTaskMappingTechnician.setStartTime(sqlTime);
                    }
                    if (assignJobToTechnician.getEndDateTime() != null) {
                        LocalDateTime ldt = LocalDateTime.parse(assignJobToTechnician.getEndDateTime(), formatter);
                        Time sqlTime = Time.valueOf(ldt.toLocalTime());
                        jobTaskMappingTechnician.setEndTime(sqlTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                    assignJobToTechnician.setTaskName(jobMappingTask.get().getTaskName());
                    assignJobToTechnician.setTaskShowId(jobMappingTask.get().getTaskShowId());
                    boolean notificationEnabled = generalSettingService.isNotificationEnabled(tenantId);
                    if(notificationEnabled){
                        applicationEventPublisher.publishEvent(new SendMailToTechnicianEvent(assignJobToTechnician, JobTaskMappingTechnician, loggedInUserEmail, tenantId, isSuperAdmin));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//                throw new CodeException("Job Task Already Assigned to Technician", ErrorCode.COMMON);
        } else {
            throw new CodeException("Technician Not Found", ErrorCode.COMMON);
        }
    }

    public boolean checkIfTaskAssignedToTechnician(JobDTO.AssignJobToTechnician assignJobToTechnician) {
        List<JobTaskMappingTechnician> byTechnicianId =
                jobTaskMappingTechnicianRepository.findByTechnicianId(assignJobToTechnician.getTechnicianId());

        LocalDate newStartDate = LocalDate.parse(assignJobToTechnician.getStartDate());
        LocalDate newEndDate = LocalDate.parse(assignJobToTechnician.getEndDate());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        LocalDateTime startDateTime = LocalDateTime.parse(assignJobToTechnician.getStartDateTime(), formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(assignJobToTechnician.getEndDateTime(), formatter);

        LocalTime newStartTime = startDateTime.toLocalTime();
        LocalTime newEndTime = endDateTime.toLocalTime();


        if (byTechnicianId != null && !byTechnicianId.isEmpty()) {
            for (JobTaskMappingTechnician existing : byTechnicianId) {

                if ("Completed".equalsIgnoreCase(existing.getTaskStatus())) {
                    continue;
                }

                LocalDate exStartDate = existing.getStartDate();
                LocalDate exEndDate = existing.getEndDate();
                if (existing.getStartTime() == null || existing.getEndTime() == null) {
                    continue;
                }
                LocalTime exStartTime = existing.getStartTime().toLocalTime();
                LocalTime exEndTime = existing.getEndTime().toLocalTime();

                boolean dateOverlap =
                        !(newEndDate.isBefore(exStartDate) || newStartDate.isAfter(exEndDate));

                if (!dateOverlap) {
                    continue;
                }

                boolean timeOverlap =
                        !(newEndTime.isBefore(exStartTime) || newStartTime.isAfter(exEndTime));

                if (timeOverlap) {
                    return false;
                }
            }
        }
        return true;
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
                    //job.setFrontOfficeId("");
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

                Optional<Job> jobOptional = jobRepository.findByUuidAndTenantIdAndDeletedFalse(leaveJob.getJobId(), tenantId);
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
        size = generalSettingService.getPageSize(tenantId);
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
//        if (TextUtils.isEmpty(addJobDTO.getJobStatus()))
//            throw new CodeException("Job Status is required", ErrorCode.COMMON);
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
//        if (TextUtils.isEmpty(addJobDTO.getJobEndDate()))
//            throw new CodeException("Job End Date is required", ErrorCode.COMMON);
        if (TextUtils.isEmpty(addJobDTO.getLeadSourceId()))
            throw new CodeException("Lead Source is required", ErrorCode.COMMON);
//        if (TextUtils.isEmpty(addJobDTO.getBudget()))
//            throw new CodeException("Budget is required", ErrorCode.COMMON);
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
        List<InventoryRequest> inventoryRequests = inventoryRequestRepository.findByTaskId(taskId);
        List<InventoryRequestResponseDTO> dtoList = inventoryRequests.stream().map(this::toDto).collect(Collectors.toList());

        List<JobDTO.DetailsForTechnician> detailsList = buildTechnicianJobTaskDetails(List.of(taskMapping), "", userName, tenantId);
        if (!detailsList.isEmpty()) {
            detailsList.get(0).setInventoryList(dtoList);
        }
        return detailsList.isEmpty() ? null : detailsList.get(0);
    }

    private InventoryRequestResponseDTO toDto(InventoryRequest request) {
        InventoryRequestResponseDTO dto = new InventoryRequestResponseDTO();
        dto.setId(request.getUuid());
        dto.setTechnicianId(request.getTechnicianId());
        dto.setTaskId(request.getTaskId());
        dto.setComment(request.getComment());
        dto.setRequestShowId(request.getRequestShowId());
        dto.setRequestedAt(request.getRequestedAt().toString());
        dto.setApprovedAt(request.getApprovedAt() != null ? request.getApprovedAt().toString() : null);
        dto.setApprovedBy(request.getApprovedBy());
        dto.setStatus(request.getStatus());
        dto.setRejectionReason(request.getRejectionReason());
        List<InventoryRequestResponseDTO.Item> items = request.getItems().stream()
                .map(item -> {
                    InventoryRequestResponseDTO.Item i =
                            new InventoryRequestResponseDTO.Item();
                    i.setInventoryListId(item.getInventoryListId());
                    i.setInventoryName(item.getInventoryName());
                    i.setRequestedQty(item.getRequestedQty());
                    i.setApprovedQty(item.getApprovedQty());
                    return i;
                })
                .collect(Collectors.toList());
        dto.setItems(items);
        return dto;
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
                            List<JobStatusMaster> jobStatus = jobStatusMasterRepository.findByName(currentTask.getJobTaskStatus());
                            if (!jobStatus.isEmpty()) {
                                jobOptional.get().setJobStatusMaster(jobStatus.get(0));
                                jobOptional.get().setJobStatus(jobStatus.get(0).getName());
                            }
                        }
                        jobRepository.save(jobOptional.get());
                    }
                }

            }
            jobTaskMappingTechnicianRepository.save(taskMappingTechnician);
            try {
                boolean notificationEnabled = generalSettingService.isNotificationEnabled(tenantId);
                if(notificationEnabled){
                    applicationEventPublisher.publishEvent(new SendMailAndPushEvent(taskMappingTechnician, tenantId, userName));
                }
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
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
        DateTimeFormatter dateFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
        for (JobTaskMappingTechnician taskMapping : taskMappings) {
            Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(taskMapping.getJobTaskMappingId());
            if (jobMappingTask.isPresent()) {
                Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(jobMappingTask.get().getJob().getUuid());
                Optional<JobTask> jobTask = jobTaskRepository.findByUuid(jobMappingTask.get().getTaskId());
                JobDTO.DetailsForTechnician details = new JobDTO.DetailsForTechnician();

                if (job.isPresent() && jobTask.isPresent()) {
                    if (!TextUtils.isEmpty(job.get().getFrontOfficeId())) {
                        FrontOfficeStaffDTO.list frontOfficeResponseData = adminClientService.getFrontOfficeById(job.get().getFrontOfficeId(), tenantId);
                        if (frontOfficeResponseData != null) {
                            details.setFrontOfficeName(frontOfficeResponseData.getName());
                            details.setFrontOfficeId(frontOfficeResponseData.getId());
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
                    if(job.get().getJobStartDate() != null){
                        details.setJobStartDate(dateFormatter != null ? job.get().getJobStartDate().format(dateFormatter) : job.get().getJobStartDate().toString());
                    }
                    if(job.get().getJobEndDate() != null){
                        details.setJobEndDate(dateFormatter != null ? job.get().getJobEndDate().format(dateFormatter) : job.get().getJobEndDate().toString());
                    }
                    details.setTaskDescription(jobTask.get().getDescription());
                    details.setJobTitle(jobTask.get().getName());
                    details.setJobDescription(job.get().getJobDescription());
                    if(taskMapping.getStartDate() != null){
                        details.setStartDate(dateFormatter != null ? taskMapping.getStartDate().format(dateFormatter) : taskMapping.getStartDate().toString());
                    }
                    if(taskMapping.getEndDate() != null){
                        details.setEndDate(dateFormatter != null ? taskMapping.getEndDate().format(dateFormatter) : taskMapping.getEndDate().toString());
                    }
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
                        CustomerDTO.GetDetails customerById = adminClientService.getCustomerById(job.get().getCustomerId());
                        if (customerById != null ){
                            details.setCustomerId(customerById.getId());
                            details.setCustomerName(customerById.getName());
                            details.setEmail(customerById.getEmail());
                            details.setMobileNumber(customerById.getMobileNumber());
                            details.setLocation(customerById.getAddress());
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
                            document.setDocumentTypeId(documents1.getDocumentTypeId());
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
                            if(htmlFormPage.getCreatedAt() != null){
                                htmlFormDTO.setCreatedAt(dateTimeFormatter != null ? htmlFormPage.getCreatedAt().format(dateTimeFormatter) : htmlFormPage.getCreatedAt().toString());
                            }
                            htmlFormDTO.setFormId(htmlFormPage.getFormId() != null ? htmlFormPage.getFormId() : "");
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
            String loggedInUserEmail, Long tenantId) throws CodeException {
        try {
            GenericSpecificationsBuilder<JobTaskMappingTechnician> builder = new GenericSpecificationsBuilder<>();
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("deleted", false));
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("technicianId", technicianId));
           // filterRequest.setPage(generalSettingService.getPageSize(tenantId));
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

            List<JobDTO.DetailsForTechnician> responseList = buildTechnicianJobTaskDetails(filteredList, filterRequest.getTxt(), loggedInUserEmail, tenantId);

            return new PageItem<>(pagedResult.getTotalPages(), responseList.size(), responseList, page, limit);

        } catch (Exception e) {
            logger.error("Error getting job tasks for technician: {}", e.getMessage(), e);
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public List<JobStatusDetail> getAllJobStatus(Long tenantId, boolean isSuperAdmin) {

        List<JobStatusMaster> statusMasters;
        //Long tenantIdToUse = isSuperAdmin ? 1L : tenantId;
        statusMasters = jobStatusMasterRepository.findDistinctNamesByDeletedFalse();

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
                                            detail.getContent().replace("{{API_URL}}", finalApiUrl + "?formId=" + detail.getId())
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
        htmlFormPage.setFormId(add.getFormId());
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
                        f.getUpdatedAt().toString(),
                        f.getFormId()
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
            Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(updateJobTaskDetails.getTaskId());
            if (jobMappingTask.isEmpty())
                throw new CodeException("Job Task mapping not found", ErrorCode.BAD_REQUEST);
            jobMappingTask.get().setJobTaskStatus("COMPLETED");
            jobMappingTaskRepository.save(jobMappingTask.get());
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
//            if (TextUtils.isEmpty(updateJobTaskDetails.getNote()))
//                throw new CodeException("Note is required to update the task details", ErrorCode.BAD_REQUEST);
            if (!TextUtils.isEmpty(updateJobTaskDetails.getNote())){
                jobMappingTask.get().setNote(updateJobTaskDetails.getNote());
            }
            jobMappingTask.get().setJobTaskStatus("COMPLETED");
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
                        List<JobStatusMaster> jobStatus = jobStatusMasterRepository.findByName(currentTask.getJobTaskStatus());
                        if (!jobStatus.isEmpty()) {
                            jobOptional.get().setJobStatusMaster(jobStatus.get(0));
                            jobOptional.get().setJobStatus(jobStatus.get(0).getName());
                        }
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

            List<TechnicianDTO.GetDetails> techDetails = technicianClientService.getTechniciansList(availableIds,tenantId);
            if(techDetails != null){
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

            List<TodayScheduleDTO> results = new ArrayList<>();
            List<TechnicianDTO.GetDetails> techDetails = technicianClientService.getTechniciansList(new ArrayList<>(technicianIds),tenantId);
            if(techDetails != null){
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
            List<JobTaskListDTO> results = new ArrayList<>();

            List<TechnicianDTO.GetDetails> techDetails = technicianClientService.getTechniciansList(new ArrayList<>(technicianIds),tenantId);
            if(techDetails != null){
                Map<String, TechnicianDTO.GetDetails> techMap = techDetails.stream()
                        .collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));
                DateTimeFormatter dateFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
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
                    dto.setStartDate(dateFormatter != null ? record.getStartDate().format(dateFormatter) : record.getStartDate().toString());
                    dto.setEndDate(dateFormatter != null ? record.getEndDate().format(dateFormatter) : record.getEndDate().toString());
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
            //listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
            Page<Job> pagedResult = getJobMappingData(listRequest, tenantId, isSuperAdmin);
            List<Job> jobs = pagedResult.getContent();

            Set<String> customerIds = jobs.stream()
                    .map(Job::getCustomerId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<CustomerDTO.GetDetails> customerDetails = new ArrayList<>();
            customerDetails = adminClientService.getCustomerList(new ArrayList<>(customerIds), tenantId, isSuperAdmin);

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

            List<TechnicianDTO.GetDetails> techDetails = technicianClientService.getTechniciansList(new ArrayList<>(technicianIds),tenantId);

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
            DateTimeFormatter dateFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
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
                if(job.getJobStartDate() != null){
                    dto.setStartDate(dateFormatter != null ? job.getJobStartDate().format(dateFormatter) : job.getJobStartDate().toString());
                }
                if(job.getJobEndDate() != null){
                    dto.setEndDate(dateFormatter != null ? job.getJobEndDate().format(dateFormatter) : job.getJobEndDate().toString());
                }
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
                DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
                JobMappingTask entity = byUuid.get();
                TaskManagerDTO dto = new TaskManagerDTO();
                dto.setTaskStatus(entity.getJobTaskStatus());
                dto.setTaskName(entity.getTaskName());
                dto.setTaskId(entity.getTaskId());
                dto.setTaskShowId(entity.getTaskShowId());
                dto.setDate(dateTimeFormatter != null ? entity.getCreatedAt().format(dateTimeFormatter) : entity.getCreatedAt().toString());
                //dto.setTime(null);
                dto.setDescription("Description");
                dto.setJobId(entity.getJob().getJobId());
                dto.setJobUuid(entity.getJob().getUuid());
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
        //listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
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
        if (listRequest.getTechnicianId() == null)
            listRequest.setTechnicianId(new ArrayList<>());
        GenericSpecificationsBuilder<JobTaskMappingTechnician> builder = new GenericSpecificationsBuilder<>();
        //listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
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
        if (listRequest.getTechnicianId().isEmpty()) {
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
            List<TechnicianDTO.GetDetails> techDetails = technicianClientService.getTechniciansList(new ArrayList<>(listRequest.getTechnicianId()),tenantId);
            if(techDetails != null) {
                techDetailsList.addAll(techDetails);
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

            // Preload job types once
            Set<String> jobTypeIds = tasks.stream()
                    .map(t -> t.getJob().getJobTypeId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());


            Map<String, JobType> jobTypeMap =
                    jobTypeRepository.findByUuidIn(jobTypeIds).stream()
                            .collect(Collectors.toMap(JobType::getUuid, jt -> jt));


            final Map<String, CustomerDTO.GetDetails> customerToNameMap = new HashMap<>();

            List<CustomerDTO.GetDetails> customerDetails = adminClientService.getCustomerList(new ArrayList<>(allCustomerIds), tenantId, false);
            if(!customerDetails.isEmpty()){
                customerDetails.forEach(t -> customerToNameMap.put(t.getId(), t));
            }
            // 3. Fetch Tag Master Data
            List<JobTag> jobTags = jobTagRepository.findByUuidIn(new ArrayList<>(allTagIds));

            final Map<String, JobTag> tagIdToNameMap = jobTags.stream()
                    .collect(Collectors.toMap(JobTag::getUuid, jt -> jt));
            DateTimeFormatter dateFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
            DateTimeFormatter timeFormatter = generalSettingService.buildTenantTimeFormatter(tenantId);
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
                        if(tech.getStartDate() != null){
                            dto.setStartDate(dateFormatter != null ? tech.getStartDate().format(dateFormatter) : tech.getStartDate().toString());
                        }
                        if(tech.getEndDate() != null){
                            dto.setEndDate(dateFormatter != null ? tech.getEndDate().format(dateFormatter) : tech.getEndDate().toString());
                        }
                        dto.setStartTime(tech.getStartTime() != null ? tech.getStartTime().toLocalTime().format(timeFormatter) : null);
                        dto.setEndTime(tech.getEndTime() != null ? tech.getEndTime().toLocalTime().format(timeFormatter) : null);
                        dto.setJobId(job.getJobId());
                        dto.setCustomerId(job.getCustomerId());
                        dto.setServiceLocation(job.getServiceLocation());
                        dto.setJobTypeId(job.getJobTypeId());
                        JobType type = jobTypeMap.get(job.getJobTypeId());
                        dto.setJobTypeName(type != null ? type.getName() : null);
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
            DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
            response.setJobNotes(
                    jobNotes.stream()
                            .map(n -> {
                                JobFullNotesDTO.JobNotesDTO dto = new JobFullNotesDTO.JobNotesDTO();
                                dto.setId(n.getUuid());
                                dto.setNote(n.getNotes());
                                dto.setCreatedAt(n.getCreatedAt() != null ? n.getCreatedAt().format(dateTimeFormatter) : null);
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
                taskDto.setCreatedAt(task.getCreatedAt() != null ? task.getCreatedAt().format(dateTimeFormatter) : null);
                JobTaskMappingTechnician tech =
                        technicians.stream()
                                .filter(x -> x.getJobTaskMappingId().equals(task.getUuid()))
                                .findFirst()
                                .orElse(null);

                if (tech != null) {

                    JobFullNotesDTO.TechnicianNotesDTO techDto = new JobFullNotesDTO.TechnicianNotesDTO();
                    techDto.setTechnicianId(tech.getTechnicianId());
                    techDto.setTaskNote(tech.getNote());
                    techDto.setCreatedAt(tech.getCreatedAt() != null ? tech.getCreatedAt().format(dateTimeFormatter) : null);
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
                                detail.getContent().replace("{{API_URL}}", finalApiUrl + "?formId=" + detail.getId())
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

    @Override
    public ResponseEntity<com.octal.fsm.common.ApiResponse> getJobTaskMapping(String id, Long tenantId, boolean isSuperAdmin) {
        try {
            Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(id);
            if (jobMappingTask.isPresent()) {
                Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(jobMappingTask.get().getJob().getUuid());
                Optional<JobTask> jobTask = jobTaskRepository.findByUuid(jobMappingTask.get().getTaskId());
                Optional<JobTaskMappingTechnician> taskMapping = jobTaskMappingTechnicianRepository.findByJobTaskMappingId(jobMappingTask.get().getUuid());
                JobDTO.TechnicianForFrontOffice details = new JobDTO.TechnicianForFrontOffice();
                if (job.isPresent() && jobTask.isPresent() && taskMapping.isPresent()) {
                    if (TextUtils.isEmpty(job.get().getFrontOfficeId())) {
                        FrontOfficeStaffDTO.list frontOfficeResponseData = adminClientService.getFrontOfficeById(job.get().getFrontOfficeId(), tenantId);
                        if (frontOfficeResponseData != null) {
                            details.setFrontOfficeName(frontOfficeResponseData.getName());
                            details.setFrontOfficeId(frontOfficeResponseData.getId());
                        }
                    }
//
                    String jobId = job.get().getJobId() != null ? job.get().getJobId().toLowerCase() : "";
                    String taskShowId = jobMappingTask.get().getTaskShowId() != null ? jobMappingTask.get().getTaskShowId().toLowerCase() : "";
                    DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
                    DateTimeFormatter dateFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
                    DateTimeFormatter timeFormatter = generalSettingService.buildTenantTimeFormatter(tenantId);
                    details.setId(taskMapping.get().getUuid());
                    details.setTaskName(jobTask.get().getName());
                    details.setNote(taskMapping.get().getTechnicianNote());
                    details.setFrontOfficeNote(taskMapping.get().getNote());
                    String customerFeedbackLink = clientFeedbackLink
                            .replace("<jobId>", job.get().getJobId())
                            .replace("<taskId>", jobMappingTask.get().getTaskShowId())
                            .replace("<technicianId>", taskMapping.get().getTechnicianId())
                            .replace("<customerId>", job.get().getCustomerId());
                    details.setClientFeedbackUrl(customerFeedbackLink);
                    if (!TextUtils.isEmpty(taskMapping.get().getSignature()))
                        details.setSignature(taskMapping.get().getSignature());
                    if (!TextUtils.isEmpty(taskMapping.get().getCancelReason()))
                        details.setCancelReason(taskMapping.get().getCancelReason());
                    if (!TextUtils.isEmpty(taskMapping.get().getDrawingJson()))
                        details.setDrawingJsonData(taskMapping.get().getDrawingJson());
                    if (!TextUtils.isEmpty(taskMapping.get().getDrawingImage()))
                        details.setDrawingImage(taskMapping.get().getDrawingImage());
                    details.setTaskId(jobMappingTask.get().getTaskShowId());
                    details.setAssignType(jobMappingTask.get().getAssignType().toString());
                    details.setTaskDescription(jobTask.get().getDescription());
                    details.setJobDescription(job.get().getJobDescription());
                    if(taskMapping.get().getStartDate() != null){
                        details.setStartDate(dateFormatter != null ? taskMapping.get().getStartDate().format(dateFormatter) : taskMapping.get().getStartDate().toString());
                    }
                    if(taskMapping.get().getEndDate() != null){
                        details.setEndDate(dateFormatter != null ? taskMapping.get().getEndDate().format(dateFormatter) : taskMapping.get().getEndDate().toString());
                    }
                    if(taskMapping.get().getStartTime() != null){
                        details.setStartTime(timeFormatter != null  ? taskMapping.get().getStartTime().toLocalTime().format(timeFormatter) : taskMapping.get().getStartTime().toString());
                    }
                    if(taskMapping.get().getEndTime() != null){
                        details.setEndTime(timeFormatter != null  ? taskMapping.get().getEndTime().toLocalTime().format(timeFormatter) : taskMapping.get().getEndTime().toString());
                    }
                    details.setServiceLocationLat(job.get().getServiceLocationLat());
                    details.setServiceLocationLng(job.get().getServiceLocationLng());
                    if (taskMapping.get().getTaskStatus().equalsIgnoreCase("ASSIGNED")) {
                        details.setStatus("NEW");
                    } else {
                        details.setStatus(taskMapping.get().getTaskStatus());
                    }

                    // Get job type

                    // Get customer details

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
                    if (!TextUtils.isEmpty(taskMapping.get().getDocuments())) {
                        try {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<String>>() {
                            }.getType();
                            List<String> documentList = gson.fromJson(taskMapping.get().getDocuments(), listType);
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
                    List<Documents> documentsList = documentsRepository.findByAttachTypeId(taskMapping.get().getJobTaskMappingId());
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
                    if (!taskMapping.get().getHtmlFormPages().isEmpty()) {
                        List<HTMLFormDTO.Details> list = new ArrayList<>();
                        for (HTMLFormPage htmlFormPage : taskMapping.get().getHtmlFormPages()) {
                            HTMLFormDTO.Details htmlFormDTO = new HTMLFormDTO.Details();
                            htmlFormDTO.setId(htmlFormPage.getUuid());
                            htmlFormDTO.setContent(htmlFormPage.getContent());
                            htmlFormDTO.setActive(htmlFormPage.getActive());
                            htmlFormDTO.setCreatedAt(htmlFormPage.getCreatedAt() != null ? htmlFormPage.getCreatedAt().format(dateTimeFormatter) : null);
                            list.add(htmlFormDTO);
                        }
                        details.setFormList(list);
                    }
                    return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Job Mapping Task fetched successfully", details, "200", HttpStatus.OK), HttpStatus.OK);
                } else if (job.isPresent() && jobTask.isPresent()) {
                    details.setId(jobMappingTask.get().getUuid());
                    details.setTaskName(jobMappingTask.get().getTaskName());
                    details.setNote(jobMappingTask.get().getNote());
                    details.setFrontOfficeNote(jobMappingTask.get().getNote());
                    details.setTaskId(jobMappingTask.get().getTaskShowId());
                    details.setTaskDescription(jobTask.get().getDescription());
                    details.setAssignType(jobMappingTask.get().getAssignType().toString());
                    return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Job Mapping Task fetched successfully", details, "200", HttpStatus.OK), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Mapping data Not Available", null, "200", HttpStatus.OK), HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, "Job Mapping Task Not Available", null, "500", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, exception.getMessage(), null, "500", HttpStatus.OK), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<com.octal.fsm.common.ApiResponse> updateDrawingData(JobDTO.UpdateDrawingDetails details, Long tenantId, boolean isSuperAdmin) {
        try {
            Optional<JobTaskMappingTechnician> byUuidAndDeletedFalse = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(details.getTaskId());
            if (byUuidAndDeletedFalse.isPresent()) {
                JobTaskMappingTechnician entity = byUuidAndDeletedFalse.get();
                entity.setDrawingJson(details.getDrawingJsonData());
                entity.setDrawingImage(awsS3BaseUrl + details.getDrawingImage());
                JobTaskMappingTechnician save = jobTaskMappingTechnicianRepository.save(entity);
                return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Drawing data updated successfully", save.getUuid(), "200", HttpStatus.OK), HttpStatus.OK);
            }
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Job Task Mapping Technician Not Available", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, exception.getMessage(), null, "500", HttpStatus.OK), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<com.octal.fsm.common.ApiResponse> getJobByCustomerId(com.octal.fsm.models.request.PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {
        if (listRequest.getCustomerId() != null) {
            //listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
            Page<Job> pagedResult = getJobMappingData(listRequest, tenantId, isSuperAdmin);
            List<Job> jobs = pagedResult.getContent();

            if (jobs.isEmpty()) {
                return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Job data fetch successfully", Collections.emptyList(), "200", HttpStatus.OK), HttpStatus.OK);
            }
            List<String> jobTypeIds = jobs.stream().map(Job::getJobTypeId).distinct().collect(Collectors.toList());
            List<JobType> byUuidAndDeletedFalse = jobTypeRepository.findByUuidAndDeletedFalse(jobTypeIds);
            Map<String, JobType> jobTypeMap = byUuidAndDeletedFalse.stream()
                    .collect(Collectors.toMap(JobType::getUuid, jt -> jt));
            DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
            DateTimeFormatter dateFormatter = generalSettingService.buildTenantDateFormatter(tenantId);
            List<JobDTO.JobCustomerDTO> jobCustomerDTOList = jobs.stream()
                    .map(job -> {
                        JobDTO.JobCustomerDTO dto = new JobDTO.JobCustomerDTO();

                        JobType jobType = jobTypeMap.get(job.getJobTypeId());

                        dto.setJobType(jobType != null ? jobType.getName() : null);
                        dto.setJobId(job.getJobId());
                        dto.setId(job.getUuid());
                        dto.setStatus(job.getJobStatus());
                        dto.setCreatedAt(job.getCreatedAt() != null ? job.getCreatedAt().format(dateTimeFormatter) : null);
                        if(job.getJobStartDate() != null){
                            dto.setStartDate(dateFormatter != null ? job.getJobStartDate().format(dateFormatter) : job.getJobStartDate().toString());
                        }
                        if(job.getJobEndDate() != null){
                            dto.setEndDate(dateFormatter != null ? job.getJobEndDate().format(dateFormatter) : job.getJobEndDate().toString());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());

            PageItem<JobDTO.JobCustomerDTO> jobCustomerDTOPageItem = new PageItem<>(
                    pagedResult.getTotalPages(),
                    pagedResult.getTotalElements(),
                    jobCustomerDTOList,
                    listRequest.getPageNumber(),
                    listRequest.getPageSize()
            );
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Job data fetch successfully", jobCustomerDTOPageItem, "200", HttpStatus.OK), HttpStatus.OK);
        }
        return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Job Not Available", null, "200", HttpStatus.OK), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<com.octal.fsm.common.ApiResponse> getJobMappingTaskByTechnician(com.octal.fsm.models.request.PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {

        if (listRequest.getTechnicianId() == null || listRequest.getTechnicianId().isEmpty()) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, "Technician Id is required", null, "500", HttpStatus.BAD_REQUEST), HttpStatus.OK);
        }
        //listRequest.setPageSize(generalSettingService.getPageSize(tenantId));
        Page<JobTaskMappingTechnician> pagedResult = getJobTaskMappingData(listRequest, tenantId, isSuperAdmin);
        List<JobTaskMappingTechnician> jobTaskMappingTechnicians = pagedResult.getContent();
        if (jobTaskMappingTechnicians.isEmpty()) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Data fetch successfully", Collections.emptyList(), "200", HttpStatus.OK), HttpStatus.OK);
        }

        List<String> mappingIds = jobTaskMappingTechnicians.stream()
                .map(JobTaskMappingTechnician::getJobTaskMappingId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());

        List<JobMappingTask> jobMappingTasks = jobMappingTaskRepository.findByUuidIn(mappingIds);

        List<String> jobTypeIds = jobMappingTasks.stream()
                .map(task -> task.getJob().getJobTypeId())
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());

        List<JobType> byUuidAndDeletedFalse = jobTypeRepository.findByUuidAndDeletedFalse(jobTypeIds);
        Map<String, JobType> jobTypeMap = byUuidAndDeletedFalse.stream()
                .collect(Collectors.toMap(JobType::getUuid, jt -> jt, (existing, replacement) -> existing));

        Map<String, JobMappingTask> technicianTaskMap = jobMappingTasks.stream()
                .collect(Collectors.toMap(JobMappingTask::getUuid, jt -> jt, (existing, replacement) -> existing));
        DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
        List<JobDTO.JobMappingTaskTechnician> responseList = jobTaskMappingTechnicians.stream()
                .map(task -> {
                    JobMappingTask jobMappingTask = technicianTaskMap.get(task.getJobTaskMappingId());
                    Job job = jobMappingTask.getJob();
                    JobDTO.JobMappingTaskTechnician dto = new JobDTO.JobMappingTaskTechnician();

                    dto.setJobId(job.getJobId());

                    JobType jobType = jobTypeMap.get(job.getJobTypeId());
                    dto.setJobType(jobType != null ? jobType.getName() : null);

                    dto.setId(jobMappingTask.getUuid());
                    dto.setTaskShowId(jobMappingTask.getTaskShowId());
                    dto.setTaskName(jobMappingTask.getTaskName());

                    dto.setTaskStatus(task.getTaskStatus());
                    if(job.getCreatedAt() != null){
                        dto.setCreatedAt(job.getCreatedAt().format(dateTimeFormatter));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        PageItem<JobDTO.JobMappingTaskTechnician> jobMappingTaskTechnicianPageItem = new PageItem<>(
                pagedResult.getTotalPages(),
                pagedResult.getTotalElements(),
                responseList,
                listRequest.getPageNumber(),
                listRequest.getPageSize()
        );
        return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Data fetch successfully", jobMappingTaskTechnicianPageItem, "200", HttpStatus.OK), HttpStatus.OK);
    }

    @Override
    public void updateInvoiceDetails(InvoiceRestDTO.Add add) throws CodeException {
        if(add.getInvoiceId() != null){
            Optional<JobInvoice> byUuid = jobInvoiceRepository.getByUuid(add.getInvoiceId());
            if(byUuid.isPresent() && add.getRefId() != null){
                JobInvoice jobInvoice = byUuid.get();
                jobInvoice.setInvoiceId(add.getRefId());
                jobInvoice.setUpdatedAt(LocalDateTime.now());
                jobInvoiceRepository.save(jobInvoice);
            }
        }
    }

    @Override
    @Transactional
    public ResponseEntity<com.octal.fsm.common.ApiResponse> addTaskFromJob(JobTaskDTO.AddWithJobDetails withJobDetails, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            if (withJobDetails == null) {
                throw new CodeException("Request body cannot be null", ErrorCode.COMMON);
            }
            if (withJobDetails.getJobId() == null) {
                throw new CodeException("Job Id is required", ErrorCode.COMMON);
            }
            if (withJobDetails.getJobTypeId() == null) {
                throw new CodeException("JobTypeId is required", ErrorCode.COMMON);
            }
            if (withJobDetails.getName() == null || withJobDetails.getName().isBlank()) {
                throw new CodeException("Task name is required", ErrorCode.COMMON);
            }
            Job job = jobRepository.findByUuidAndDeletedFalse(withJobDetails.getJobId()).
                    orElseThrow(() -> new CodeException("Job not found", ErrorCode.COMMON));
            JobType jobType = jobTypeRepository.findByUuid(withJobDetails.getJobTypeId()).
                    orElseThrow(() -> new CodeException("Job type not found", ErrorCode.COMMON));
            JobStatusMaster jobStatusMaster = jobStatusMasterRepository.findByUuid(withJobDetails.getStatusMasterId())
                    .orElseThrow(() -> new CodeException("Job status not found", ErrorCode.COMMON));

            //Create JobTask entity
            JobTask jobTask = new JobTask();
            jobTask.setName(withJobDetails.getName());
            jobTask.setDescription(withJobDetails.getDescription());
            jobTask.setAssignedType(withJobDetails.getAssignedType());
            jobTask.setSequence(withJobDetails.getPreviousTaskSequence() == null ? 1 : withJobDetails.getPreviousTaskSequence() + 1);
            jobTask.setJobType(jobType);
            jobTask.setJobStatusMaster(jobStatusMaster);
            //Save JobTask
            JobTask savedTask = jobTaskRepository.save(jobTask);
            //save job mapping task
            if (savedTask.getSequence() == 1) {
                job.setCurrentTaskId(savedTask.getUuid());
                job.setJobStatusMaster(savedTask.getJobStatusMaster());
                job.setJobStatus(savedTask.getName());
            }
            JobMappingTask task = new JobMappingTask();
            task.setTaskId(savedTask.getUuid());
            task.setTaskName(savedTask.getName());
            task.setTaskShowId(codeGenerator.generateTaskId());
            task.setTaskSequence(savedTask.getSequence());
            task.setJobTaskStatus(savedTask.getJobStatusMaster().getName());
            task.setAssignType(savedTask.getAssignedType());
            task.setJob(job);
            job.getJobMappingTasks().add(task);
            Job save = jobRepository.save(job);
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Task saved successfully", savedTask.getUuid(), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, e.getMessage(), null, "500", HttpStatus.OK), HttpStatus.OK);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<com.octal.fsm.common.ApiResponse> removeTask(String taskId, Long tenantId, boolean isSuperAdmin) throws CodeException {
        try {
            if (taskId == null || taskId.isBlank()) {
                throw new CodeException("TaskId is required", ErrorCode.COMMON);
            }
            JobMappingTask mappingTask = jobMappingTaskRepository.findByUuidWithJob(taskId)
                                        .orElseThrow(() ->new CodeException("Task not found", ErrorCode.COMMON));

            Job job = mappingTask.getJob();
            Integer deletedSequence = mappingTask.getTaskSequence();

            mappingTask.setDeleted(true);

            job.getJobMappingTasks().stream()
                    .filter(t -> !Boolean.TRUE.equals(t.isDeleted()))
                    .filter(t -> t.getTaskSequence() > deletedSequence)
                    .forEach(t -> t.setTaskSequence(t.getTaskSequence() - 1));

            if (mappingTask.getTaskId().equals(job.getCurrentTaskId())) {
                JobMappingTask nextTask = job.getJobMappingTasks().stream()
                                .filter(t -> !Boolean.TRUE.equals(t.isDeleted()))
                                .min(Comparator.comparing(JobMappingTask::getTaskSequence))
                                .orElse(null);

                if (nextTask != null) {
                    JobTask nextJobTask = jobTaskRepository.findByUuidAndDeletedFalse(nextTask.getTaskId()).orElse(null);
                    if(nextJobTask != null){
                        job.setCurrentTaskId(nextJobTask.getUuid());
                        job.setJobStatus(nextJobTask.getName());
                        job.setJobStatusMaster(nextJobTask.getJobStatusMaster());
                    }
                } else {
                    job.setCurrentTaskId(null);
                }
            }
            jobRepository.save(job);
            jobMappingTaskRepository.save(mappingTask);
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Task Removed successfully", "", "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, e.getMessage(), null, "500", HttpStatus.OK), HttpStatus.OK);
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

    @Override
    @Transactional(readOnly = true)
    public List<JobDetailsForInventory> getAllJobByTechnicianId(String technicianId, String loggedInUserEmail, Long tenantId, Boolean isSuperAdmin) throws CodeException {

        if (technicianId == null || technicianId.isBlank()) {
            return Collections.emptyList();
        }
        return jobTaskMappingTechnicianRepository.findTaskDetailsByTechnicianId(technicianId);
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

        if (listRequest.getTechnicianId() != null && !listRequest.getTechnicianId().isEmpty()) {
            builder.with(jobTaskMappingTechnicianSpecificationFactory.isEqual("technicianId", listRequest.getTechnicianId().get(0)));
        }

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

        if (listRequest.getCustomerId() != null) {
            builder.with(jobSpecificationFactory.isEqual("customerId", listRequest.getCustomerId()));
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

    //    public PageItem<TaskManagerDTO> generateDataUsingSpecification(String technicianId, com.octal.fsm.models.request.PageRequest.List listReq, Long tenantId) {
//        try {
//            List<JobTaskMappingTechnician> jobTaskMappingTechnicians = jobTaskMappingTechnicianRepository.findByTechnicianId(technicianId);
//
//            Set<String> mappingIds = jobTaskMappingTechnicians.stream().map(JobTaskMappingTechnician::getJobTaskMappingId).collect(Collectors.toSet());
//            String trimmedText = listReq.getSearchText().trim();
//            listReq.setSearchText(trimmedText);
//            GenericSpecificationsBuilder<JobMappingTask> builder = new GenericSpecificationsBuilder<>();
//            Pageable pageable = null;
//            if (Boolean.TRUE.equals(listReq.getAsc())) {
//                pageable = org.springframework.data.domain.PageRequest.of(listReq.getPageNumber(), listReq.getPageSize(), Sort.by(listReq.getShortingField()).ascending());
//            } else {
//                pageable = org.springframework.data.domain.PageRequest.of(listReq.getPageNumber(), listReq.getPageSize(), Sort.by(listReq.getShortingField()).descending());
//            }
//            Page<JobMappingTask> pageData = new PageImpl<>(Collections.emptyList(), pageable, 0);
//            if (!TextUtils.isEmpty(technicianId) && !jobTaskMappingTechnicians.isEmpty()) {
//                prepareTechnicianTaskFilters(listReq, builder, technicianId, tenantId, new ArrayList<>(mappingIds));
//                pageData = jobMappingTaskRepository.findAll(builder.build(), pageable);
//            }
//            if (TextUtils.isEmpty(technicianId)) {
//                prepareTechnicianTaskFilters(listReq, builder, technicianId, tenantId, new ArrayList<>(mappingIds));
//                pageData = jobMappingTaskRepository.findAll(builder.build(), pageable);
//            }
//            List<TechnicianDTO.GetDetails> techDetails;
//            ApiResponse technicianResponse = technicianClient.getTechByIds(jobTaskMappingTechnicians.stream().map(JobTaskMappingTechnician::getTechnicianId).collect(Collectors.toList()), tenantId).getBody();
//            if (technicianResponse != null && technicianResponse.getStatus() != null && technicianResponse.getStatus().equalsIgnoreCase("200") && technicianResponse.getData() != null) {
//                techDetails = objectMapper.convertValue(
//                        technicianResponse.getData(),
//                        new TypeReference<List<TechnicianDTO.GetDetails>>() {
//                        }
//                );
//            }
//
//            List<TaskManagerDTO> responseList = new ArrayList<>();
//            for (JobMappingTask department : pageData.getContent()) {
//                JobTaskMappingTechnician taskMappingTechnician=jobTaskMappingTechnicians.stream().filter(obj->obj.getJobTaskMappingId().equalsIgnoreCase(department.getUuid())).findFirst().get();
//                TechnicianDTO.GetDetails getDetails=techDetails.stream().filter(obj->obj.getId().equals(taskMappingTechnician.getTechnicianId())).findFirst();
//                TaskManagerDTO dto = new TaskManagerDTO();
//                dto.setJobId(department.getJob().getUuid());
//                dto.setTaskId(department.getUuid());
//                dto.setTaskShowId(department.getTaskShowId());
//                dto.setTaskName(department.getTaskName());
//                dto.setTaskStatus(department.getJobTaskStatus());
//                dto.setDate(String.valueOf(department.getCreatedAt()));
//                dto.setDescription("Description");
//                dto.setStartTime(taskMappingTechnician.getStartTime().toString());
//                dto.setEndTime(taskMappingTechnician.getEndTime().toString());
//                dto.setTechnicianName(getDetails.getName());
//                responseList.add(dto);
//            }
//            return new PageItem<>(pageData.getTotalPages(), pageData.getTotalElements(), responseList, listReq.getPageNumber(),
//                    listReq.getPageSize());
//        } catch (Exception exception) {
//            throw new RuntimeException(exception);
//        }
//
//    }
    public PageItem<TaskManagerDTO> generateDataUsingSpecification(List<String> technicianIds, com.octal.fsm.models.request.PageRequest.List listReq, Long tenantId) {
        try {
            listReq.setPageSize(generalSettingService.getPageSize(tenantId));
            if (technicianIds == null) {
                technicianIds = new ArrayList<>();
            }
            listReq.setSearchText(listReq.getSearchText().trim());
            List<JobTaskMappingTechnician> techMappings = new ArrayList<>();
            Set<String> mappingIds = new HashSet<>();
            // 1. Fetch technician mappings
            if (!technicianIds.isEmpty()) {
                techMappings = jobTaskMappingTechnicianRepository.findByTechnicianIdIn(technicianIds);
                if (techMappings == null || techMappings.isEmpty()) {
                    return new PageItem<>(0, 0, new ArrayList<TaskManagerDTO>(), listReq.getPageNumber(), listReq.getPageSize());
                }
                mappingIds = techMappings.stream().map(JobTaskMappingTechnician::getJobTaskMappingId).collect(Collectors.toSet());
            }
            // 2. Prepare Pageable
            Sort sort = Boolean.TRUE.equals(listReq.getAsc()) ? Sort.by(listReq.getShortingField()).ascending() : Sort.by(listReq.getShortingField()).descending();

            Pageable pageable = PageRequest.of(listReq.getPageNumber(), listReq.getPageSize(), sort);

            // 3. Prepare spec builder
            GenericSpecificationsBuilder<JobMappingTask> builder = new GenericSpecificationsBuilder<>();

            if (!technicianIds.isEmpty()) {
                prepareTechnicianTaskFilters(listReq, builder, technicianIds, tenantId, new ArrayList<>(mappingIds));
            } else if (technicianIds.isEmpty()) {
                prepareTechnicianTaskFilters(listReq, builder, null, tenantId, new ArrayList<>(Objects.requireNonNull(mappingIds)));
            }
            Page<JobMappingTask> pageData = jobMappingTaskRepository.findAll(builder.build(), pageable);
            if (techMappings.isEmpty()) {
                techMappings = jobTaskMappingTechnicianRepository.findByJobTaskMappingIdIn(pageData.getContent().stream().map(AbstractPersistable::getUuid).collect(Collectors.toList()));
            }
            // 4. Build technician details lookup map
            Map<String, TechnicianDTO.GetDetails> technicianMap;
            if (!techMappings.isEmpty()) {
                List<TechnicianDTO.GetDetails> techDetails = technicianClientService.getTechniciansList(new ArrayList<>(technicianIds), tenantId);
                if (techDetails != null) {
                    technicianMap = techDetails.stream().collect(Collectors.toMap(TechnicianDTO.GetDetails::getId, t -> t));
                } else {
                    technicianMap = Collections.emptyMap();
                }
            } else {
                technicianMap = Collections.emptyMap();
            }
            // Build mapping -> technicianTime lookup to avoid repeated stream operations later
            Map<String, JobTaskMappingTechnician> mappingTechMap = techMappings.stream().collect(Collectors.toMap(JobTaskMappingTechnician::getJobTaskMappingId, m -> m));
            final Map<String, CustomerDTO.GetDetails> customerToNameMap = new HashMap<>();

            List<CustomerDTO.GetDetails> customerList = adminClientService.getCustomerList(new ArrayList<>(pageData.getContent().stream().map(task -> task.getJob().getCustomerId()).collect(Collectors.toList())), tenantId, false);
            if(customerList != null){
                customerList.forEach(t -> customerToNameMap.put(t.getId(), t));
            }
            List<JobType> jobTypeList = jobTypeRepository.findByUuidAndDeletedFalse(pageData.getContent().stream().map(task -> task.getJob().getJobTypeId()).collect(Collectors.toList()));
            Map<String, String> jobTypeMap = jobTypeList.stream().collect(Collectors.toMap(JobType::getUuid, JobType::getName));

            // 5. Prepare DTO response
            DateTimeFormatter timeFormatter = generalSettingService.buildTenantTimeFormatter(tenantId);
            DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
            List<TaskManagerDTO> responseList = pageData.getContent().stream()
                    .map(task -> {
                        JobTaskMappingTechnician tech = mappingTechMap.get(task.getUuid());
                        TechnicianDTO.GetDetails details = (tech != null)
                                ? technicianMap.get(tech.getTechnicianId())
                                : null;

                        TaskManagerDTO dto = new TaskManagerDTO();
                        dto.setJobId(task.getJob().getJobId());
                        dto.setTaskId(task.getUuid());
                        dto.setTaskShowId(task.getTaskShowId());
                        dto.setTaskName(task.getTaskName());
                        dto.setTaskStatus(task.getJobTaskStatus());
                        dto.setDate(task.getCreatedAt() != null ? task.getCreatedAt().format(dateTimeFormatter) : null);
                        dto.setCustomerName(
                                customerToNameMap.containsKey(task.getJob().getCustomerId())
                                        ? customerToNameMap.get(task.getJob().getCustomerId()).getName()
                                        : "N/A"
                        );
                        //dto.setTime(null);
                        dto.setJobTypeName(jobTypeMap.get(task.getJob().getJobTypeId()));
                        dto.setDescription("Description");

                        if (tech != null) {
                            dto.setStartTime(tech.getStartTime() != null ? tech.getStartTime().toLocalTime().format(timeFormatter) : null);
                            dto.setEndTime(tech.getEndTime() != null ? tech.getEndTime().toLocalTime().format(timeFormatter) : null);
                        }

                        dto.setTechnicianName(details != null ? details.getName() : "Assigned to CSR");

                        return dto;
                    }).collect(Collectors.toList());

            return new PageItem<>(pageData.getTotalPages(), pageData.getTotalElements(), responseList, listReq.getPageNumber(), listReq.getPageSize());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    private void prepareTechnicianTaskFilters(com.octal.fsm.models.request.PageRequest.List listReq, GenericSpecificationsBuilder<JobMappingTask> builder,
                                              List<String> frontOfficeId, Long tenantId, List<String> jobTaskMappingIds) {

        builder.with(jobMappingTaskSpecificationFactory.joinEqualsLong("job", "tenantId", tenantId));
        if (!TextUtils.isEmpty(listReq.getJobTypeId())) {
            builder.with(jobMappingTaskSpecificationFactory.joinEquals("job", "jobTypeId", listReq.getJobTypeId()));
        }
        if (!TextUtils.isEmpty(listReq.getCustomerId())) {
            builder.with(jobMappingTaskSpecificationFactory.joinEquals("job", "customerId", listReq.getCustomerId()));
        }

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
                            .or(jobMappingTaskSpecificationFactory.like("taskShowId", listReq.getSearchText()));
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
            listReq.setPageSize(generalSettingService.getPageSize(tenantId));
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
            final Map<String, CustomerDTO.GetDetails> customerToNameMap = new HashMap<>();

            ApiResponse customerResponse = adminClient.getCustomerByIds(new ArrayList<>(page.getContent().stream().map(task -> task.getJob().getCustomerId()).collect(Collectors.toList())), tenantId, false).getBody();

            if (customerResponse != null && "200".equalsIgnoreCase(customerResponse.getStatus()) && customerResponse.getData() != null) {
                List<CustomerDTO.GetDetails> customerDetails = objectMapper.convertValue(
                        customerResponse.getData(),
                        new TypeReference<List<CustomerDTO.GetDetails>>() {
                        }
                );
                customerDetails.forEach(t -> customerToNameMap.put(t.getId(), t));
                // List<JobType>jobTypeList=jobTypeRepository.findByUuidAndDeletedFalse()
            }
            DateTimeFormatter dateTimeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
            List<JobType> jobTypeList = jobTypeRepository.findByUuidAndDeletedFalse(page.getContent().stream().map(task -> task.getJob().getJobTypeId()).collect(Collectors.toList()));
            Map<String, String> jobTypeMap = jobTypeList.stream()
                    .collect(Collectors.toMap(JobType::getUuid, JobType::getName));
            for (JobMappingTask department : page.getContent()) {
                TaskManagerDTO dto = new TaskManagerDTO();
                dto.setJobId(department.getJob().getJobId());
                dto.setTaskShowId(department.getTaskShowId());
                dto.setTaskId(department.getUuid());
                dto.setTaskName(department.getTaskName());
                dto.setTaskStatus(department.getJobTaskStatus());
                dto.setDate(department.getCreatedAt() != null ? department.getCreatedAt().format(dateTimeFormatter) : null);
                dto.setDescription("Description");
                dto.setCustomerName(
                        customerToNameMap.containsKey(department.getJob().getCustomerId())
                                ? customerToNameMap.get(department.getJob().getCustomerId()).getName()
                                : "N/A"
                );
                //dto.setTime(null);
                dto.setJobTypeName(jobTypeMap.get(department.getJob().getJobTypeId()));
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

        if (!TextUtils.isEmpty(listReq.getJobTypeId())) {
            builder.with(jobMappingTaskSpecificationFactory.joinEquals("job", "jobTypeId", listReq.getJobTypeId()));
        }
        if (!TextUtils.isEmpty(listReq.getCustomerId())) {
            builder.with(jobMappingTaskSpecificationFactory.joinEquals("job", "customerId", listReq.getCustomerId()));
        }

        builder.with(jobMappingTaskSpecificationFactory.isEqual("assignType", TaskAssignedType.CSR));//for CSR

        builder.with(jobMappingTaskSpecificationFactory.isEqual("deleted", false));

        if (!TextUtils.isEmpty(listReq.getJobStatus())) {
            builder.with(jobMappingTaskSpecificationFactory.isEqual("jobTaskStatus", listReq.getTaskStatus()));
        }

        if (org.apache.commons.lang.StringUtils.isNotBlank(listReq.getSearchText())) {
            builder.with(jobMappingTaskSpecificationFactory.like("taskName", listReq.getSearchText()).
                    or(jobMappingTaskSpecificationFactory.like("taskShowId", listReq.getSearchText())));
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