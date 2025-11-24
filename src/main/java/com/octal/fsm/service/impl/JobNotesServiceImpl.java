package com.octal.fsm.service.impl;

import com.octal.fsm.dto.*;
import com.octal.fsm.entities.JobNotes;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.models.request.PageRequest;
import com.octal.fsm.repositories.JobNotesRepository;
import com.octal.fsm.service.JobNotesService;
import com.octal.fsm.specification.GenericSpecificationsBuilder;
import com.octal.fsm.specification.SpecificationFactory;
import com.octal.fsm.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobNotesServiceImpl implements JobNotesService {

    @Autowired
    private JobNotesRepository jobNotesRepository;

    @Autowired
    private SpecificationFactory<JobNotes> jobNotesSpecificationFactory;

    @Override
    public String addNotes(JobNotesDTO.Add addNotes, boolean isSuperAdmin) throws CodeException {
        try{
            if(TextUtils.isEmpty(addNotes.getJobId()))
                throw new CodeException("Job id is required to save job notes", ErrorCode.COMMON);
            JobNotes jobNotes = new JobNotes();
            jobNotes.setNotes(addNotes.getNotes());
            jobNotes.setJobId(addNotes.getJobId());
            jobNotes.setCreatedAt(LocalDateTime.now());
            jobNotes.setCreatedBy(addNotes.getCreatedBy());
            JobNotes save = jobNotesRepository.save(jobNotes);
            return save.getUuid();
        }catch (Exception exception){
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public List<JobNotesDTO.Details> getJobNotesByJobId(String jobId) {
        List<JobNotes> byJobId = jobNotesRepository.findByJobId(jobId);
        return byJobId.stream().map(f -> {
            JobNotesDTO.Details dto = new JobNotesDTO.Details();
            dto.setId(f.getUuid());
            dto.setNotes(f.getNotes());
            dto.setJobId(f.getJobId());
            dto.setCreatedBy(f.getCreatedBy());
            dto.setActive(f.getActive());
            dto.setCreatedAt(f.getCreatedAt().toString());
            dto.setUpdatedAt(f.getUpdatedAt().toString());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public PageItem<JobNotesDTO.Details> getAllJobNotes(PageRequest.List listRequest) {
        String trimmedText = listRequest.getSearchText().trim();
        listRequest.setSearchText(trimmedText);
        GenericSpecificationsBuilder<JobNotes> builder = new GenericSpecificationsBuilder<>();
        Pageable pageable = null;
        if (Boolean.TRUE.equals(listRequest.getAsc())) {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).ascending());
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(listRequest.getPageNumber(), listRequest.getPageSize(), Sort.by(listRequest.getShortingField()).descending());
        }
        builder.with(jobNotesSpecificationFactory.isEqual("deleted", false));
        if(listRequest.getJobId() != null && !listRequest.getJobId().isEmpty()){
            builder.with(jobNotesSpecificationFactory.isEqual("jobId", listRequest.getJobId()));
        }

        Page<JobNotes> pagedResult =  jobNotesRepository.findAll(builder.build(), pageable);
        List<JobNotesDTO.Details> responseList = new ArrayList<>();
        for (JobNotes notes : pagedResult.getContent()) {
            JobNotesDTO.Details dto = new JobNotesDTO.Details();
            dto.setId(notes.getUuid());
            dto.setNotes(notes.getNotes());
            dto.setJobId(notes.getJobId());
            dto.setCreatedBy(notes.getCreatedBy());
            dto.setCreatedAt(String.valueOf(notes.getCreatedAt()));
            dto.setUpdatedAt(String.valueOf(notes.getUpdatedAt()));
            dto.setActive(notes.getActive());
            responseList.add(dto);
        }
        return new PageItem<>(pagedResult.getTotalPages(), pagedResult.getTotalElements(), responseList, listRequest.getPageNumber(),
                listRequest.getPageSize());
    }
}
