package com.octal.fsm.service;


import com.octal.fsm.dto.JobTagDTO;
import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;

public interface JobTagService {

    String addJobTag(JobTagDTO.Add add,Long tenantId, Boolean isSuperAdmin) throws CodeException;

    Boolean deleteById(String id) throws CodeException;

    JobTypeDTO.Detail getJobTagByUuid(String id,Long tenantId, Boolean isSuperAdmin) throws CodeException;

    Boolean changeStatus(String id) throws CodeException;

    PageItem<JobTagDTO.Detail> getAllJobTags(PageRequest.List listRequest,Long tenantId, Boolean isSuperAdmin);

}
