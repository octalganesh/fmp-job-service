package com.octal.fsm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.ipp.util.StringUtils;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.dto.PaymentListRequestDTO;
import com.octal.fsm.dto.PaymentResponseDTO;
import com.octal.fsm.dto.QuickBooksInvoiceResponse;
import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobInvoice;
import com.octal.fsm.entities.JobType;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.repositories.JobInvoiceRepository;
import com.octal.fsm.repositories.JobRepository;
import com.octal.fsm.repositories.JobTypeRepository;
import com.octal.fsm.service.JobInvoiceService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private JobInvoiceRepository jobInvoiceRepository;

    @Autowired
    private JobTypeRepository jobTypeRepository;

    @Autowired
    private JobRepository jobRepository;

    public PageItem<PaymentResponseDTO> getInvoiceDataofFrontOfficeUser(String userId, PaymentListRequestDTO request, String dateFormat) {
        Sort sort = "asc".equalsIgnoreCase(request.getSortOrder()) ? Sort.by(request.getSortField()).ascending() : Sort.by(request.getSortField()).descending();
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(), sort);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StringUtils.hasText(dateFormat) ? dateFormat : "yyyy-MM-dd");
        List<String> jobIds = jobRepository.findAllByFrontOfficeIdAndDeletedFalse(userId).stream().map(Job::getUuid).collect(Collectors.toList());
        if (jobIds.isEmpty()) {
            return new PageItem<>(0, 0, new ArrayList<>(), request.getPageNumber(), request.getPageSize());
        }

        GenericSpecificationsBuilder<JobInvoice> builder = new GenericSpecificationsBuilder<>();
        builder.with(jobInvoiceSpecificationFactory.isEqual("deleted", false));
        builder.with(jobInvoiceSpecificationFactory.in("jobId", jobIds));
//        if (StringUtils.hasText(request.getSearchText())) {
//            builder.with(jobInvoiceSpecificationFactory.like("invoiceId", request.getSearchText()));
//        }
        Map<String, Object> filters = request.getFilters();
        if (filters != null) {
            Map<String, String> dateRange = (Map<String, String>) filters.get("dateRange");
            if (dateRange != null) {
                String startDate = dateRange.get("startDate");
                String endDateStr  = dateRange.get("endDate");

                if (StringUtils.hasText(startDate)) {
                    builder.with(jobInvoiceSpecificationFactory.isGreaterThanOrEquals("createdAt", LocalDate.parse(startDate).atStartOfDay()));
                }
                LocalDate endDate = null;
                if (StringUtils.hasText(endDateStr)) {
                    endDate = LocalDate.parse(endDateStr);
                } else if (startDate != null) {
                    endDate = LocalDate.now();
                }

                if (endDate != null) {
                    builder.with(jobInvoiceSpecificationFactory.isLessThanOrEquals("createdAt", endDate.atTime(23, 59, 59)));
                }
            }
        }

        Page<JobInvoice> pagedResult = jobInvoiceRepository.findAll(builder.build(), pageable);
        List<PaymentResponseDTO> responseList = pagedResult.getContent().stream()
                .map(invoice -> mapToPaymentResponseDTO(invoice, formatter))
                .collect(Collectors.toList());
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, request.getPageNumber(), request.getPageSize());
    }

    private PaymentResponseDTO mapToPaymentResponseDTO(JobInvoice invoice, DateTimeFormatter formatter) {
        if (invoice == null) return null;
        PaymentResponseDTO dto = new PaymentResponseDTO();

        dto.setId(invoice.getUuid());
        dto.setJobId(invoice.getJobId());
        dto.setTaskId(null); // not available

        Optional<Job> job = jobRepository.findByUuidAndDeletedFalse(invoice.getJobId());
        if (job.isPresent()) {
            dto.setJobId(job.get().getJobId());
            Optional<JobType> jobType = jobTypeRepository.findByUuid(job.get().getJobTypeId());
            jobType.ifPresent(type -> dto.setJobType(type.getName()));
        }

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
        if(invoice.getResponseDTO()!=null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                QuickBooksInvoiceResponse qb = mapper.readValue(invoice.getResponseDTO(), QuickBooksInvoiceResponse.class);

                if (qb == null || qb.getInvoice() == null) {
                    return dto;
                }

                QuickBooksInvoiceResponse.Invoice qbInvoice = qb.getInvoice();
                if (qbInvoice.getCustomerRef() != null) {
                    dto.setCustomerName(qbInvoice.getCustomerRef().getName());
                }

                Double total = qbInvoice.getTotalAmt();
                Double balance = qbInvoice.getBalance();

                if (total != null && balance != null) {
                    dto.setTotalPaymentPending(balance);
                    dto.setTotalPaymentReceived(total - balance);
                    dto.setPaymentStatus(balance > 0 ? "PENDING" : "PAID");
                }

                if (qbInvoice.getMetaData() != null) {
                    dto.setUpdatedAt(qbInvoice.getMetaData().getLastUpdatedTime());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while parsing the Quick Book Invoice Response:- " + e.getMessage());
            }
        }

        return dto;
    }
}
