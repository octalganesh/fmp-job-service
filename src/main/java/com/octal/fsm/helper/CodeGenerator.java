package com.octal.fsm.helper;

import com.octal.fsm.entities.Job;
import com.octal.fsm.repositories.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

@Component
public class CodeGenerator {


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


    private String getCode(String prefix) {
        Random r = new Random(System.currentTimeMillis());
        int id = ((1 + r.nextInt(2)) * 1000000 + r.nextInt(1000000));
        String codePrefix = prefix;
        return codePrefix + id;
    }



}
