package com.octal.fsm.helper;

import com.octal.fsm.repositories.JobMappingTaskRepository;
import com.octal.fsm.repositories.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class CodeGenerator {

    @Autowired
    private JobMappingTaskRepository jobMappingTaskRepository;

    @Autowired
    private JobRepository jobRepository;


    public String getJobId() {
        String code = "";
        Boolean job;
        do {
            String newCode = getCode("JOB");
            job = jobRepository.existsByJobId(newCode);
            if (!job) {
                code = newCode;
            }
        } while (job);
        return code;
    }

    public String generateTaskId() {
        String code = "";
        Boolean task;
        do {
            String newCode = getCode("TASK");
            task = jobMappingTaskRepository.existsByTaskShowId(newCode);
            if (!task) {
                code = newCode;
            }
        } while (task);
        return code;
    }


    private String getCode(String prefix) {
        Random r = new Random(System.currentTimeMillis());
        int id = ((1 + r.nextInt(2)) * 1000000 + r.nextInt(1000000));
        String codePrefix = prefix;
        return codePrefix + id;
    }

    private static final SecureRandom secureRandom = new SecureRandom();

    public String generateCustomerCode() {
        int number = secureRandom.nextInt(100_000_000); // 0 to 99,999,999
        return "CUST_" + String.format("%08d", number);
    }



}
