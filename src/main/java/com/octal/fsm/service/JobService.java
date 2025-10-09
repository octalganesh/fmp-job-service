package com.octal.fsm.service;

import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;

public interface JobService {

    String addJob(JobDTO.Add addJobDTO) throws CodeException;

    PageItem<JobDTO.JobListResponse> getAllJobs(int page, int size, String sortBy, Boolean order, String jobType, String jobStatus, String jobTag, Double serviceLocationLat, Double serviceLocationLng, String customerType, String fromStartDate, String toStartDate, String loggedInUserEmail) throws CodeException;

    //    JobDTO.Detail updateJob(JobDTO.Update updateJobDTO) throws CodeException;
//
//    Boolean deleteJob(String id) throws CodeException;
//
    JobDTO.Detail getJobById(String id, String loggedInUserEmail) throws CodeException;

    PageItem<JobDTO.JobTaskListResponse> getJobTask(int page, int size, String sortBy, Boolean order, String jobId, String loggedInUserEmail) throws CodeException;

    void assignJobToTechnician(JobDTO.AssignJobToTechnician assignJobToTechnician, String loggedInUserEmail) throws CodeException;


    PageItem<JobDTO.DetailsForTechnician> getJobTasksForTechnician(JobDTO.JobFilterRequest filterRequest, String technicianId, String loggedInUserEmail) throws CodeException;


    JobDTO.DetailsForTechnician getJobTaskDetailsForTechnician(String technicianId, String taskId, String userName) throws CodeException;
}
