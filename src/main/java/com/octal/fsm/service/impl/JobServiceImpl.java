package com.octal.fsm.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.clients.TechnicianClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.entities.*;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.listener.events.SendMailToTechnicianEvent;
import com.octal.fsm.repositories.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private JobService jobService;
    @Autowired
    private DocumentsRepository documentsRepository;
    @Value("${aws.base-url}")
    private String awsS3BaseUrl;
    @Value("${client.feedback.link}")
    private String clientFeedbackLink;

    @Override
    public String addJob(JobDTO.Add addJobDTO) throws CodeException {
        try {
            validatedJobDTO(addJobDTO);
            return jobTransformer.transformToEntity(addJobDTO); // Using getRecordId() instead of getId()
        } catch (Exception e) {
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

    @Override
    public void createUpFrontInvoice(JobDTO.CreateUpFrontInvoiceRequest createUpFrontInvoice) throws CodeException {
        Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(createUpFrontInvoice.getJobId());
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
    public void updateJobTags(String jobId, JobDTO.UpdateJobTags updateJobTags, String loggedInUserEmail) throws CodeException {
        Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(jobId);
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
    public PageItem<JobDTO.JobListResponse> getAllJobs(int page, int size, String sortBy, Boolean order, String jobType, String jobStatus, String jobTag, Double serviceLocationLat, Double serviceLocationLng, String customerType, String fromStartDate, String toStartDate, String loggedInUserEmail, String location) throws CodeException {
        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(order)) {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(page, size, Sort.by(sortBy).descending());
        }
        builder.with(jobSpecificationFactory.isEqual("deleted", false));
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
                ApiResponse apiResponse = adminClient.getJobDetailsWithLeadAndCustomerDetails(job.getCustomerId(), job.getLeadSourceId(), loggedInUserEmail).getBody();
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
    public JobDTO.Detail getJobById(String id, String loggedInUserEmail) throws CodeException {
        Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(id);
        if (jobOpt.isEmpty())
            throw new CodeException("Job Not Found", ErrorCode.COMMON);
        Job job = jobOpt.get();
        JobDTO.Detail response = new JobDTO.Detail();
        response.setId(id);
        response.setJobId(job.getJobId());
        Optional<JobType> jobType = jobTypeRepository.findByUuid(job.getJobTypeId());
        if (jobType.isPresent()) {
            response.setJobTypeId(jobType.get().getUuid());
            response.setJobType(jobType.get().getName());
        }
        if (!TextUtils.isEmpty(job.getLeadSourceId()) || !TextUtils.isEmpty(job.getCustomerTypeId()) || !TextUtils.isEmpty(job.getCustomerId())) {
            try {
                ApiResponse apiResponse = adminClient.getJobDetailsForCustomerInfo(job.getCustomerId(), job.getLeadSourceId(), job.getCustomerTypeId(), loggedInUserEmail).getBody();
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
    public void assignJobToTechnician(JobDTO.AssignJobToTechnician assignJobToTechnician, String loggedInUserEmail) throws CodeException {
        Boolean jobExist = jobRepository.existsByUuidAndDeletedFalse(assignJobToTechnician.getJobId());
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
                jobTaskMappingTechnicianRepository.save(jobTaskMappingToTechnician.get());

                Gson gson = new Gson();
                if (assignJobToTechnician.getDocuments() != null && !assignJobToTechnician.getDocuments().isEmpty()) {
                    List<String> documentsWithUrl = assignJobToTechnician.getDocuments().stream()
                            .map(doc -> awsS3BaseUrl + doc)  // Prepending AWS base URL
                            .collect(Collectors.toList());
                    jobTaskMappingToTechnician.get().setDocuments(gson.toJson(documentsWithUrl));
                }
                JobDTO.Detail jobDetails = jobService.getJobById(assignJobToTechnician.getJobId(), loggedInUserEmail);

                // Convert response data to TechnicianDTO.GetDetails
                if (jobDetails != null) {
                    String jsonResponse = gson.toJson(technicianResponse.getData());
                    TechnicianDTO.TechnicianData getDetails = gson.fromJson(jsonResponse, TechnicianDTO.TechnicianData.class);

                    if (getDetails != null && getDetails.getEmail() != null) {
                        applicationEventPublisher.publishEvent(new SendMailToTechnicianEvent(getDetails, jobDetails, loggedInUserEmail));
                    }
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
                JobDTO.Detail jobDetails = jobService.getJobById(assignJobToTechnician.getJobId(), loggedInUserEmail);

                // Convert response data to TechnicianDTO.GetDetails
                if (jobDetails != null) {
                    String jsonResponse = gson.toJson(technicianResponse.getData());
                    TechnicianDTO.TechnicianData getDetails = gson.fromJson(jsonResponse, TechnicianDTO.TechnicianData.class);

                    if (getDetails != null && getDetails.getEmail() != null) {
                        applicationEventPublisher.publishEvent(new SendMailToTechnicianEvent(getDetails, jobDetails, loggedInUserEmail));
                    }
                }
            }
//                throw new CodeException("Job Task Already Assigned to Technician", ErrorCode.COMMON);
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

//    @Override
//    public JobDTO.Detail updateJob(JobDTO.Update updateJobDTO) throws CodeException {
//        try {
//            Optional<Job> existingJobOpt = jobRepository.findByUuidAndDeletedFalse(updateJobDTO.getId());
//            if (existingJobOpt.isEmpty()) {
//                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
//            }
//
//            Job existingJob = existingJobOpt.get();
//            Job updatedJob = jobTransformer.updateEntityFromDTO(updateJobDTO, existingJob);
//            Job savedJob = jobRepository.save(updatedJob);
//
//            return jobTransformer.transformToDetailDTO(savedJob);
//        } catch (Exception e) {
//            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
//        }
//    }
//
//    @Override
//    public Boolean deleteJob(String id) throws CodeException {
//        try {
//            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(id);
//            if (jobOpt.isEmpty()) {
//                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
//            }
//
//            Job job = jobOpt.get();
//            job.setDeleted(true);
//            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
//            job.setUpdatedAt(LocalDateTime.now());
//            jobRepository.save(job);
//
//            return true;
//        } catch (Exception e) {
//            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
//        }
//    }
//
//    @Override
//    public JobDTO.Detail getJobById(String id) throws CodeException {
//        try {
//            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(id);
//            if (jobOpt.isEmpty()) {
//                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
//            }
//
//            return jobTransformer.transformToDetailDTO(jobOpt.get());
//        } catch (Exception e) {
//            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
//        }
//    }
//
//    @Override
//    public Boolean changeJobStatus(String id, String status) throws CodeException {
//        try {
//            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(id);
//            if (jobOpt.isEmpty()) {
//                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
//            }
//
//            Job job = jobOpt.get();
//            job.setJobStatus(status);
//            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
//            job.setUpdatedAt(LocalDateTime.now());
//            jobRepository.save(job);
//
//            return true;
//        } catch (Exception e) {
//            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
//        }
//    }
//
//    @Override
//    public PageItem<JobDTO.List> getAllJobs(PageRequest.List listRequest) {
//        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
//        prepareJobSearchFilter(listRequest, builder);
//
//        String sortDirection = listRequest.getAsc() ? "ASC" : "DESC";
//        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), listRequest.getShortingField());
//        Pageable pageRequest = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), sort);
//
//        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageRequest);
//        List<JobDTO.List> responseList = jobTransformer.transformToListDTO(pagedResult.getContent());
//
//        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList,
//                listRequest.getPageNumber(), listRequest.getPageSize());
//    }
//
//    @Override
//    public PageItem<JobDTO.List> searchJobs(String searchTerm, PageRequest.List listRequest) {
//        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
//        prepareJobSearchFilter(listRequest, builder);
//
//        if (StringUtils.isNotBlank(searchTerm)) {
//            builder.with(jobSpecificationFactory.like("jobSummary", searchTerm));
//        }
//
//        String sortDirection = listRequest.getAsc() ? "ASC" : "DESC";
//        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), listRequest.getShortingField());
//        Pageable pageRequest = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), sort);
//
//        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageRequest);
//        List<JobDTO.List> responseList = jobTransformer.transformToListDTO(pagedResult.getContent());
//
//        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList,
//                listRequest.getPageNumber(), listRequest.getPageSize());
//    }
//
//    @Override
//    public PageItem<JobDTO.List> getJobsByStatus(String status, PageRequest.List listRequest) {
//        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
//        prepareJobSearchFilter(listRequest, builder);
//        builder.with(jobSpecificationFactory.isEqual("jobStatus", status));
//
//        String sortDirection = listRequest.getAsc() ? "ASC" : "DESC";
//        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), listRequest.getShortingField());
//        Pageable pageRequest = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), sort);
//
//        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageRequest);
//        List<JobDTO.List> responseList = jobTransformer.transformToListDTO(pagedResult.getContent());
//
//        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList,
//                listRequest.getPageNumber(), listRequest.getPageSize());
//    }
//
//    @Override
//    public PageItem<JobDTO.List> getJobsByPriority(String priority, PageRequest.List listRequest) {
//        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
//        prepareJobSearchFilter(listRequest, builder);
//        builder.with(jobSpecificationFactory.isEqual("priority", priority));
//
//        String sortDirection = listRequest.getAsc() ? "ASC" : "DESC";
//        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), listRequest.getShortingField());
//        Pageable pageRequest = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), sort);
//
//        Page<Job> pagedResult = jobRepository.findAll(builder.build(), pageRequest);
//        List<JobDTO.List> responseList = jobTransformer.transformToListDTO(pagedResult.getContent());
//
//        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList,
//                listRequest.getPageNumber(), listRequest.getPageSize());
//    }
//
//    @Override
//    public JobDTO.Detail assignTechnician(String jobId, String technicianId) throws CodeException {
//        try {
//            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(jobId);
//            if (jobOpt.isEmpty()) {
//                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
//            }
//
//            Job job = jobOpt.get();
//            // job.setAssignedTechnician(jobTransformer.getTechnicianById(technicianId));
//            job.setAssignedDateTime(LocalDateTime.now());
//            job.setJobStatus("ASSIGNED");
//            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
//            job.setUpdatedAt(LocalDateTime.now());
//
//            Job savedJob = jobRepository.save(job);
//            return jobTransformer.transformToDetailDTO(savedJob);
//        } catch (Exception e) {
//            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
//        }
//    }
//
//    @Override
//    public JobDTO.Detail updateJobProgress(String jobId, String summary, String status) throws CodeException {
//        try {
//            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(jobId);
//            if (jobOpt.isEmpty()) {
//                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
//            }
//
//            Job job = jobOpt.get();
//            job.setJobSummary(summary);
//            job.setJobStatus(status);
//            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
//            job.setUpdatedAt(LocalDateTime.now());
//
//            Job savedJob = jobRepository.save(job);
//            return jobTransformer.transformToDetailDTO(savedJob);
//        } catch (Exception e) {
//            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
//        }
//    }
//
//    @Override
//    public JobDTO.Detail completeJob(String jobId, String summary) throws CodeException {
//        try {
//            Optional<Job> jobOpt = jobRepository.findByUuidAndDeletedFalse(jobId);
//            if (jobOpt.isEmpty()) {
//                throw new CodeException(ErrorCode.RECORD_NOT_FOUND);
//            }
//
//            Job job = jobOpt.get();
//            job.setJobSummary(summary);
//            job.setJobStatus("COMPLETED");
//            // Removed setUpdatedBy since it doesn't exist in AbstractPersistable
//            job.setUpdatedAt(LocalDateTime.now());
//
//            Job savedJob = jobRepository.save(job);
//            return jobTransformer.transformToDetailDTO(savedJob);
//        } catch (Exception e) {
//            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
//        }
//    }
//
//    private void prepareJobSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<Job> builder) {
//        // Always filter out deleted records
//        builder.with(jobSpecificationFactory.isEqual("deleted", false));
//
//        // Search by text if provided
//        if (StringUtils.isNotBlank(listRequest.getSearchText())) {
//            builder.with(jobSpecificationFactory.like("jobSummary", listRequest.getSearchText()));
//        }
//
//        // Filter by date range if provided
//        if (listRequest.getStartDate() != null) {
//            builder.with(jobSpecificationFactory.isGreaterThanOrEquals("createdAt",
//                    listRequest.getStartDate().atStartOfDay()));
//        }
//
//        if (listRequest.getEndDate() != null) {
//            builder.with(jobSpecificationFactory.isLessThanOrEquals("createdAt",
//                    listRequest.getEndDate().plusDays(1).atStartOfDay()));
//        }
//    }

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
    public JobDTO.DetailsForTechnician getJobTaskDetailsForTechnician(String technicianId, String taskId, String userName) throws CodeException {
        Optional<JobTaskMappingTechnician> taskMappingOpt = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(taskId);
//        if (taskMappingOpt.isEmpty()) {
//            return new PageItem<>()
//        }
        JobTaskMappingTechnician taskMapping = taskMappingOpt.get();
        List<JobDTO.DetailsForTechnician> detailsList = buildTechnicianJobTaskDetails(List.of(taskMapping), "", userName);
        return detailsList.isEmpty() ? null : detailsList.get(0);
    }

    @Override
    public void updateJobTaskStatus(String technicianId, String taskId, String status, String note, String signature, String userName) throws CodeException {
        Optional<JobTaskMappingTechnician> jobTaskMappingTechnician = jobTaskMappingTechnicianRepository.findByUuidAndDeletedFalse(taskId);
        if (jobTaskMappingTechnician.isPresent()) {
            JobTaskMappingTechnician taskMappingTechnician = jobTaskMappingTechnician.get();
            if (taskMappingTechnician.getTaskStatus().equalsIgnoreCase("COMPLETED")) {
                throw new CodeException("Task Already Completed", ErrorCode.BAD_REQUEST);
            }
            taskMappingTechnician.setTaskStatus(status);
            if (taskMappingTechnician.getTaskStatus().equalsIgnoreCase("cancelled")) {
                if(TextUtils.isEmpty(note)){
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
            }
            jobTaskMappingTechnicianRepository.save(taskMappingTechnician);
        } else {
            throw new CodeException("Task Not Found", ErrorCode.BAD_REQUEST);
        }

    }

    private List<JobDTO.DetailsForTechnician> buildTechnicianJobTaskDetails(List<JobTaskMappingTechnician> taskMappings, String txt, String loggedInUserEmail) {
        List<JobDTO.DetailsForTechnician> responseList = new ArrayList<>();

        for (JobTaskMappingTechnician taskMapping : taskMappings) {
            Optional<JobMappingTask> jobMappingTask = jobMappingTaskRepository.findByUuid(taskMapping.getJobTaskMappingId());
            if (jobMappingTask.isPresent()) {
                Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(jobMappingTask.get().getJob().getUuid());
                Optional<JobTask> jobTask = jobTaskRepository.findByUuid(jobMappingTask.get().getTaskId());

                if (job.isPresent() && jobTask.isPresent()) {
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
                    JobDTO.DetailsForTechnician details = new JobDTO.DetailsForTechnician();
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
                    clientFeedbackLink = clientFeedbackLink
                            .replace("<jobId>", job.get().getJobId())
                            .replace("<taskId>", jobMappingTask.get().getTaskShowId())
                            .replace("<technicianId>", taskMapping.getTechnicianId())
                            .replace("<customerId>", job.get().getCustomerId());
                    details.setClientFeedbackUrl(clientFeedbackLink);
                    if (!TextUtils.isEmpty(taskMapping.getSignature()))
                        details.setSignature(taskMapping.getSignature());
                    if (!TextUtils.isEmpty(taskMapping.getCancelReason()))
                        details.setCancelReason(taskMapping.getCancelReason());
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
                        ApiResponse customerResponse = adminClient.getCustomerById(job.get().getCustomerId(), loggedInUserEmail).getBody();
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
            String loggedInUserEmail) throws CodeException {
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
                else if(filterRequest.getStatus().equalsIgnoreCase("ongoing")) {
                    Set<String> statusList = Set.of("ENROUTE", "ARRIVED", "INPROGRESS");
                    builder.with(jobTaskMappingTechnicianSpecificationFactory.fieldIn("taskStatus", statusList));
                }
                else
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

            List<JobDTO.DetailsForTechnician> responseList = buildTechnicianJobTaskDetails(filteredList, filterRequest.getTxt(), loggedInUserEmail);

            return new PageItem<>(pagedResult.getTotalPages(), responseList.size(), responseList, page, limit);

        } catch (Exception e) {
            logger.error("Error getting job tasks for technician: {}", e.getMessage(), e);
            throw new CodeException(ErrorCode.EXCEPTION_OCCUR);
        }
    }

}