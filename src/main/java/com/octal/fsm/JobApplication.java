package com.octal.fsm;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;

@SpringBootApplication()
@EnableEurekaClient
@RestController
@EnableFeignClients
public class JobApplication implements ApplicationRunner {

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication.run(JobApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }


}