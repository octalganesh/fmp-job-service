package com.octal.fsm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octal.fsm.clients.AdminClient;
import com.octal.fsm.dto.*;
import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobInvoice;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobInvoiceRepository;
import com.octal.fsm.repositories.JobRepository;
import com.octal.fsm.repositories.JobTypeRepository;
import com.octal.fsm.service.GeneralSettingService;
import com.octal.fsm.service.JobInvoiceService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobInvoiceServiceImpl implements JobInvoiceService {

    @Autowired
    private SpecificationFactory<JobInvoice> jobInvoiceSpecificationFactory;

    @Autowired
    private SpecificationFactory<Job> jobSpecificationFactory;

    @Autowired
    private JobInvoiceRepository jobInvoiceRepository;

    @Autowired
    private JobTypeRepository jobTypeRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private GeneralSettingService generalSettingService;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private ObjectMapper objectMapper;


    public PageItem<PaymentResponseDTO> getInvoiceDataOfFrontOfficeUser(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {

        if (listRequest.getFrontOfficeId() == null) {
            throw new CodeException("Frontoffice id required", ErrorCode.COMMON);
        }

        // get All Jobs
        List<String> jobIds = getAllJobs(listRequest).stream().map(Job::getJobId).collect(Collectors.toList());

        if (jobIds.isEmpty()) {
            return new PageItem<>(0, 0, new ArrayList<>(), listRequest.getPageNumber(), listRequest.getPageSize());
        }
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }

        // Get Job Invoice
        GenericSpecificationsBuilder<JobInvoice> builder = new GenericSpecificationsBuilder<>();
        prepareInvoiceListSearchFilter(listRequest, builder, jobIds, tenantId);
        Page<JobInvoice> pagedResult = jobInvoiceRepository.findAll(builder.build(), pageable);
        DateTimeFormatter timeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
        List<PaymentResponseDTO> responseList = pagedResult.getContent().stream()
                .map(invoice -> mapToPaymentResponseDTO(listRequest, invoice, timeFormatter))
                .collect(Collectors.toList());
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(), listRequest.getPageSize());
    }

    private List<Job> getAllJobs(PageRequest.List listRequest) {
        GenericSpecificationsBuilder<Job> builder = new GenericSpecificationsBuilder<>();
        builder.with(jobSpecificationFactory.isEqual("deleted", false));

        if (listRequest.getIsActive() != null) {
            builder.with(jobSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }

        if (listRequest.getFrontOfficeId() != null && !listRequest.getFrontOfficeId().isEmpty()) {
            builder.with(jobSpecificationFactory.isEqual("frontOfficeId", listRequest.getFrontOfficeId()));
        }

        if (listRequest.getCustomerTypeId() != null && !listRequest.getCustomerTypeId().isEmpty()) {
            builder.with(jobSpecificationFactory.isEqual("customerTypeId", listRequest.getCustomerTypeId()));
        }

        if (listRequest.getJobTypeId() != null && !listRequest.getJobTypeId().isEmpty()) {
            builder.with(jobSpecificationFactory.isEqual("jobTypeId", listRequest.getJobTypeId()));
        }

        if (listRequest.getCustomerId() != null && !listRequest.getCustomerId().isEmpty()) {
            builder.with(jobSpecificationFactory.isEqual("customerId", listRequest.getCustomerId()));
        }

        return jobRepository.findAll(builder.build());
    }

    private void prepareInvoiceListSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<JobInvoice> builder, List<String> jobIds, Long tenantId) {
        builder.with(jobInvoiceSpecificationFactory.isEqual("deleted", false));

        if (jobIds != null && !jobIds.isEmpty()) {
            builder.with(jobInvoiceSpecificationFactory.in("jobId", jobIds));
        }

        if (listRequest.getIsActive() != null) {
            builder.with(jobInvoiceSpecificationFactory.isEqual("isActive", listRequest.getIsActive()));
        }

        if (listRequest.getStartDate() != null) {
            builder.with(jobInvoiceSpecificationFactory.isGreaterThanOrEquals("createdAt", listRequest.getStartDate().atStartOfDay()));
        }

        if (listRequest.getEndDate() != null) {
            builder.with(jobInvoiceSpecificationFactory.isLessThanOrEquals("createdAt", listRequest.getEndDate().atTime(23, 59, 59)));
        }

        if ("PAID".equalsIgnoreCase(listRequest.getPaymentStatus())) {
            builder.with(jobInvoiceSpecificationFactory.isEqual("paid", true));
        }

        if(listRequest.getSearchText()!=null && !listRequest.getSearchText().isEmpty()){
            builder.with(jobInvoiceSpecificationFactory.like("jobId", listRequest.getSearchText())
                    .or(jobInvoiceSpecificationFactory.isEqual("jobId",listRequest.getSearchText()))
                    .or(jobInvoiceSpecificationFactory.isEqual("invoiceId",listRequest.getSearchText()))
                    .or(jobInvoiceSpecificationFactory.like("invoiceId",listRequest.getSearchText())));
        }
    }


    private PaymentResponseDTO mapToPaymentResponseDTO(PageRequest.List listRequest, JobInvoice invoice, DateTimeFormatter formatter) {
        if (invoice == null) return null;
        PaymentResponseDTO dto = new PaymentResponseDTO();

        dto.setId(invoice.getUuid());
        dto.setJobId(invoice.getJobId());
        dto.setTaskId(null); // not available

        Optional<Job> job = jobRepository.findByJobIdAndDeletedFalse(invoice.getJobId());
        if (job.isPresent()) {
            dto.setJobId(job.get().getJobId());
            Optional<JobType> jobType = jobTypeRepository.findByUuid(job.get().getJobTypeId());
            jobType.ifPresent(type -> dto.setJobTypeId(type.getName()));
        }

        dto.setCustomerTypeId(listRequest.getCustomerTypeId());

        dto.setPaymentId(invoice.getTxnId() != null ? invoice.getTxnId() : invoice.getInvoiceId());
        dto.setTotalPaymentAmount(invoice.getAmount());
        dto.setNotes(invoice.getNote());
        dto.setPaymentType(invoice.getPaymentType() != null ? invoice.getPaymentType() : null);

        dto.setPaymentStatus(Boolean.TRUE.equals(invoice.getPaid()) ? "PAID" : "PENDING");

        if (invoice.getCreatedAt() != null) {
            String created = invoice.getCreatedAt().format(formatter);
            dto.setCreatedAt(created);
            dto.setPaymentReceivedDate(created);
        }

        if (invoice.getUpdatedAt() != null) {
            dto.setUpdatedAt(invoice.getUpdatedAt().format(formatter));
        }

        // -------- Parse QuickBooks responseDTO --------
        if (invoice.getBalanceDue() != null && invoice.getTotalAmountWithTax() != null) {
            Double totalAmount = Double.parseDouble(invoice.getTotalAmountWithTax());
            Double balanceDue = Double.parseDouble(invoice.getBalanceDue());

            dto.setTotalPaymentAmount(totalAmount);
            dto.setTotalPaymentPending(balanceDue);
            dto.setTotalPaymentReceived(totalAmount - balanceDue);
        }

        if (listRequest.getCustomerId()!=null && !listRequest.getCustomerId().isEmpty()) {
            CustomerDTO.GetDetails customerDTO = (CustomerDTO.GetDetails) adminClient.getCustomerById(listRequest.getCustomerId()).getBody().getData();

            dto.setCustomerName(customerDTO.getName());
            dto.setLocation(customerDTO.getPrimaryLocation());
        }

//        if(invoice.getResponseDTO()!=null) {
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                QuickBooksInvoiceResponse qb = mapper.readValue(invoice.getResponseDTO(), QuickBooksInvoiceResponse.class);
//
//                if (qb == null || qb.getInvoice() == null) {
//                    return dto;
//                }
//
//                QuickBooksInvoiceResponse.Invoice qbInvoice = qb.getInvoice();
//                if (qbInvoice.getCustomerRef() != null) {
//                    dto.setCustomerName(qbInvoice.getCustomerRef().getName());
//                }
//
//                Double total = qbInvoice.getTotalAmt();
//                Double balance = qbInvoice.getBalance();
//
//                if (total != null && balance != null) {
//                    dto.setTotalPaymentPending(balance);
//                    dto.setTotalPaymentReceived(total - balance);
//                    dto.setPaymentStatus(balance > 0 ? "PENDING" : "PAID");
//                }
//
//                if (qbInvoice.getMetaData() != null) {
//                    dto.setUpdatedAt(qbInvoice.getMetaData().getLastUpdatedTime());
//                }
//            } catch (Exception e) {
//                throw new RuntimeException("Error while parsing the Quick Book Invoice Response:- " + e.getMessage());
//            }
//        }

        return dto;
    }

    private Double parseDoubleSafe(String value) {
        try {
            return value == null || value.isBlank() ? 0.0 : Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

//    public PageItem<PaymentResponseDTO> getTrxData(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {
//
//        // Fetch all Jobs based on filters
//        List<Job> jobList = getAllJobs(listRequest);
//
//        // Store UUIDs for invoice lookup
//        List<String> jobUuids = jobList.stream()
//                .map(Job::getUuid)
//                .collect(Collectors.toList());
//
//        if (jobUuids.isEmpty()) {
//            return new PageItem<>(0, 0, new ArrayList<>(), listRequest.getPageNumber(), listRequest.getPageSize());
//        }
//
//        // Map jobId -> customerId
//        Map<String, String> jobCustomerMap = jobList.stream()
//                .collect(Collectors.toMap(
//                        Job::getJobId,
//                        Job::getCustomerId,
//                        (existing, replacement) -> existing
//                ));
//
//        // Collect unique customerIds for remote fetch
//        List<String> customerIds = jobList.stream()
//                .map(Job::getCustomerId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        ApiResponse apiResponse = adminClient.getCustomerByIds(customerIds, tenantId, false).getBody();
//
//        List<CustomerDTO.GetDetails> customerList =
//                apiResponse != null && apiResponse.getData() != null
//                        ? objectMapper.convertValue(
//                        apiResponse.getData(),
//                        new TypeReference<List<CustomerDTO.GetDetails>>() {
//                        })
//                        : Collections.emptyList();
//
//        Map<String, CustomerDTO.GetDetails> customerDataMap =
//                customerList.stream()
//                        .collect(Collectors.toMap(
//                                CustomerDTO.GetDetails::getId,
//                                c -> c,
//                                (existing, replacement) -> existing
//                        ));
//
//        // Sort logic
//        Pageable pageable = Boolean.TRUE.equals(listRequest.getAsc())
//                ? org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending())
//                : org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
//
//        // Invoice Filters
//        GenericSpecificationsBuilder<JobInvoice> builder = new GenericSpecificationsBuilder<>();
//        prepareInvoiceListSearchFilter(listRequest, builder, jobUuids, tenantId);
//
//        Page<JobInvoice> pagedResult = jobInvoiceRepository.findAll(builder.build(), pageable);
//
//        DateTimeFormatter formatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
//
//        List<PaymentResponseDTO> responseList = pagedResult.getContent().stream()
//                .map(invoice -> mapToTrxResponseDTO(listRequest, invoice, formatter, jobCustomerMap, customerDataMap))
//                .collect(Collectors.toList());
//
//        double totalAmount = responseList.stream()
//                .map(PaymentResponseDTO::getTotalPaymentAmount)
//                .filter(Objects::nonNull)
//                .mapToDouble(Double::doubleValue)
//                .sum();
//
//        long totalTrx = pagedResult.getTotalElements();
//
//        PageItem<PaymentResponseDTO> pageItem =
//                new PageItem<>(
//                        pagedResult.getTotalPages(),
//                        pagedResult.getTotalElements(),
//                        responseList,
//                        listRequest.getPageNumber(),
//                        listRequest.getPageSize()
//                );
//
//        return pageItem;
//    }

    public TransactionResponseDTO getTrxData(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin) throws CodeException {

        // Fetch all Jobs based on filters
        List<Job> jobList = getAllJobs(listRequest);

        // Store UUIDs for invoice lookup
        List<String> jobUuids = jobList.stream()
                .map(Job::getJobId)
                .collect(Collectors.toList());

        if (jobUuids.isEmpty()) {
            TransactionResponseDTO emptyResp = new TransactionResponseDTO();
            emptyResp.setItems(Collections.emptyList());
            emptyResp.setTotalPages(0);
            emptyResp.setTotalItems(0L);
            emptyResp.setPageNumber(listRequest.getPageNumber());
            emptyResp.setPageSize(listRequest.getPageSize());
            emptyResp.setTotalAmount(0.0);
            return emptyResp;
        }

        // Map jobId -> customerId
        Map<String, String> jobCustomerMap = jobList.stream()
                .collect(Collectors.toMap(
                        Job::getJobId,
                        Job::getCustomerId,
                        (existing, replacement) -> existing
                ));

        // Collect unique customerIds
        List<String> customerIds = jobList.stream()
                .map(Job::getCustomerId)
                .distinct()
                .collect(Collectors.toList());

        ApiResponse apiResponse = adminClient.getCustomerByIds(customerIds, tenantId, false).getBody();

        List<CustomerDTO.GetDetails> customerList =
                apiResponse != null && apiResponse.getData() != null
                        ? objectMapper.convertValue(
                        apiResponse.getData(),
                        new TypeReference<List<CustomerDTO.GetDetails>>() {}
                )
                        : Collections.emptyList();

        Map<String, CustomerDTO.GetDetails> customerDataMap =
                customerList.stream()
                        .collect(Collectors.toMap(
                                CustomerDTO.GetDetails::getId,
                                c -> c,
                                (existing, replacement) -> existing
                        ));

        // Sort logic
        Pageable pageable = Boolean.TRUE.equals(listRequest.getAsc())
                ? org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending())
                : org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());

        // Invoice Filters
        GenericSpecificationsBuilder<JobInvoice> builder = new GenericSpecificationsBuilder<>();
        prepareInvoiceListSearchFilter(listRequest, builder, jobUuids, tenantId);

        Page<JobInvoice> pagedResult = jobInvoiceRepository.findAll(builder.build(), pageable);

        DateTimeFormatter formatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);

        List<PaymentResponseDTO> responseList = pagedResult.getContent().stream()
                .map(invoice -> mapToTrxResponseDTO(listRequest, invoice, formatter, jobCustomerMap, customerDataMap))
                .collect(Collectors.toList());

        // === Calculate Totals ===
        double totalAmount = responseList.stream()
                .map(PaymentResponseDTO::getTotalPaymentAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        TransactionResponseDTO resp = new TransactionResponseDTO();
        resp.setItems(responseList);
        resp.setTotalPages(pagedResult.getTotalPages());
        resp.setTotalItems(pagedResult.getTotalElements());
        resp.setPageNumber(listRequest.getPageNumber());
        resp.setPageSize(listRequest.getPageSize());
        resp.setTotalAmount(totalAmount);

        return resp;
    }

    private PaymentResponseDTO mapToTrxResponseDTO(PageRequest.List listRequest,
                                                   JobInvoice invoice,
                                                   DateTimeFormatter formatter,
                                                   Map<String, String> jobCustomerMap,
                                                   Map<String, CustomerDTO.GetDetails> customerDataMap) {

        if (invoice == null) return null;

        PaymentResponseDTO dto = new PaymentResponseDTO();

        dto.setId(invoice.getUuid());
        dto.setJobId(invoice.getJobId());
        dto.setTaskId(null);
        dto.setCustomerTypeId(listRequest.getCustomerTypeId());
        dto.setPaymentId(invoice.getInvoiceId());
        dto.setNotes(invoice.getNote());
        dto.setPaymentStatus(Boolean.TRUE.equals(invoice.getPaid()) ? "PAID" : "PENDING");
        dto.setTotalPaymentAmount(invoice.getAmount());

        if (invoice.getCreatedAt() != null) {
            String created = invoice.getCreatedAt().format(formatter);
            dto.setCreatedAt(created);
            dto.setPaymentReceivedDate(created);
        }

        if (invoice.getUpdatedAt() != null) {
            dto.setUpdatedAt(invoice.getUpdatedAt().format(formatter));
        }

        // Handle QuickBooks Values
        if (invoice.getBalanceDue() != null && invoice.getTotalAmountWithTax() != null) {
            double total = Double.parseDouble(invoice.getTotalAmountWithTax());
            double due = Double.parseDouble(invoice.getBalanceDue());
            dto.setTotalPaymentAmount(total);
            dto.setTotalPaymentPending(due);
            dto.setTotalPaymentReceived(total - due);
        }

        // Fetch job for jobType
        jobRepository.findByJobIdAndDeletedFalse(invoice.getJobId())
                .ifPresent(job -> {
                    dto.setJobId(job.getJobId());
                    jobTypeRepository.findByUuid(job.getJobTypeId())
                            .ifPresent(type -> dto.setJobTypeId(type.getName()));
                });

        // --- Customer Mapping ---
        String jobId = dto.getJobId();
        String customerId = jobCustomerMap.get(jobId);

        if (customerId != null) {
            CustomerDTO.GetDetails customer = customerDataMap.get(customerId);
            if (customer != null) {
                dto.setCustomerName(customer.getName());
                dto.setLocation(customer.getAddress());
            }
        }

        return dto;
    }
}
