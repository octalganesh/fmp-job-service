package com.octal.fsm.helper;

import com.octal.fsm.repositories.EstimateBillRepository;
import com.octal.fsm.repositories.InventoryRequestRepository;
import com.octal.fsm.repositories.JobMappingTaskRepository;
import com.octal.fsm.repositories.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Random;

@Component
public class CodeGenerator {

    @Autowired
    private JobMappingTaskRepository jobMappingTaskRepository;

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private InventoryRequestRepository inventoryRequestRepository;
    @Autowired
    private EstimateBillRepository estimateBillRepository;


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

    public String generateRequestShowId() {
        String code = "";
        Boolean task;
        do {
            String newCode = getCode("REQ");
            task = inventoryRequestRepository.existsByRequestShowId(newCode);
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

    public String generateEstimateId() {
        int year = LocalDate.now().getYear();
        String prefix = "EST-" + year + "-";
        // Get last used estimateId for current year
        String lastId = estimateBillRepository.findLastEstimateId(prefix);
        int nextCounter = 1;
        if (lastId != null) {
            // lastId example: EST-2026-012
            String lastNumber = lastId.substring(lastId.lastIndexOf("-") + 1);
            nextCounter = Integer.parseInt(lastNumber) + 1;
        }
        return String.format("EST-%d-%03d", year, nextCounter);
    }





}
