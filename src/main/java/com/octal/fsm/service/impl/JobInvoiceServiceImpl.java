package com.octal.fsm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.ipp.util.StringUtils;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.dto.PaymentListRequestDTO;
import com.octal.fsm.dto.PaymentResponseDTO;
import com.octal.fsm.dto.QuickBooksInvoiceResponse;
import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobInvoice;
import com.octal.fsm.entities.JobTaskMappingTechnician;
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
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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


    public PageItem<PaymentResponseDTO> getInvoiceDataOfFrontOfficeUser(PageRequest.List listRequest, Long tenantId, boolean isSuperAdmin)throws CodeException {

        if(listRequest.getFrontOfficeId() == null){
            throw new CodeException("Frontoffice id required", ErrorCode.COMMON);
        }

        // get All Jobs
        List<String> jobIds = getAllJobs(listRequest).stream().map(Job::getUuid).collect(Collectors.toList());

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
        prepareInvoiceListSearchFilter(listRequest,builder,jobIds,tenantId);
        Page<JobInvoice> pagedResult = jobInvoiceRepository.findAll(builder.build(), pageable);
        DateTimeFormatter timeFormatter = generalSettingService.buildTenantDateTimeFormatter(tenantId);
        List<PaymentResponseDTO> responseList = pagedResult.getContent().stream()
                .map(invoice -> mapToPaymentResponseDTO(listRequest, invoice, timeFormatter))
                .collect(Collectors.toList());
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(), listRequest.getPageSize());
    }

    private List<Job> getAllJobs(PageRequest.List listRequest){
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

        return jobRepository.findAll(builder.build());
    }

    private void prepareInvoiceListSearchFilter(PageRequest.List listRequest, GenericSpecificationsBuilder<JobInvoice> builder,List<String> jobIds, Long tenantId) {
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
    }


    private PaymentResponseDTO mapToPaymentResponseDTO(PageRequest.List listRequest, JobInvoice invoice, DateTimeFormatter formatter) {
        if (invoice == null) return null;
        PaymentResponseDTO dto = new PaymentResponseDTO();

        dto.setId(invoice.getUuid());
        dto.setJobId(invoice.getJobId());
        dto.setTaskId(null); // not available

        Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(invoice.getJobId());
        if (job.isPresent()) {
            dto.setJobId(job.get().getJobId());
            Optional<JobType> jobType = jobTypeRepository.findByUuid(job.get().getJobTypeId());
            jobType.ifPresent(type -> dto.setJobTypeId(type.getName()));
        }

        dto.setCustomerTypeId(listRequest.getCustomerTypeId());

        dto.setPaymentId(invoice.getInvoiceId());
        dto.setTotalPaymentAmount(invoice.getAmount());
        dto.setNotes(invoice.getNote());

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
            Double balanceDue  = Double.parseDouble(invoice.getBalanceDue());

            dto.setTotalPaymentAmount(totalAmount);
            dto.setTotalPaymentPending(balanceDue);
            dto.setTotalPaymentReceived(totalAmount - balanceDue);
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

}
