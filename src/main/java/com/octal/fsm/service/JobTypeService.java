package com.octal.fsm.service;


import com.octal.fsm.dto.JobTaskDTO;
import com.octal.fsm.dto.JobTypeDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;

import javax.validation.Valid;
import java.util.List;

public interface JobTypeService {

    String addJobType(JobTypeDTO.Add add,Long tenantId, Boolean isSuperAdmin) throws CodeException;

    void addJobTaskByJobTypeId(JobTaskDTO.Add add) throws CodeException;

    Boolean deleteById(String id) throws CodeException;

    JobTypeDTO.Detail getJobTypeByUuid(String id,Long tenantId, Boolean isSuperAdmin) throws CodeException;

    Boolean changeStatus(String id) throws CodeException;

    PageItem<JobTypeDTO.Detail> getAllJobTypes(PageRequest.List listRequest,Long tenantId, Boolean isSuperAdmin);


    List<JobTypeDTO.Detail> getAllJobs();

    PageItem<JobTypeDTO.DetailWithoutJobTasks> getAllJobTypesForTechnician(PageRequest.@Valid List listRequest,Long tenantId, Boolean isSuperAdmin);
}
