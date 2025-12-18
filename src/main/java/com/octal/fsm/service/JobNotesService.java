package com.octal.fsm.service;

import com.octal.fsm.dto.JobCallDTO;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.JobNotesDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface JobNotesService {

    String addNotes(JobNotesDTO.Add addNotes, boolean isSuperAdmin) throws CodeException;

    List<JobNotesDTO.Details> getJobNotesByJobId(String jobId);

    PageItem<JobNotesDTO.Details> getAllJobNotes(PageRequest.List listRequest,Long tenantId);


}
