package com.octal.fsm.service;


import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;

import java.util.List;

public interface JobTypeService {

    String addJobType(JobTypeDTO.Add add) throws CodeException;

    Boolean deleteById(String id) throws CodeException;

    JobTypeDTO.Detail getJobTypeByUuid(String id) throws CodeException;

    Boolean changeStatus(String id) throws CodeException;

    PageItem<JobTypeDTO.Detail> getAllJobTypes(PageRequest.List listRequest);


    List<JobTypeDTO.Detail> getAllJobs();
}
