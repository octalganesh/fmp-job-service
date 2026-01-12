package com.octal.fsm.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "sendMailToTechnicianEvent")
    public Executor sendMailToTechnicianEvent() {
        return new ThreadPoolTaskExecutor();
    }
    @Bean(name = "sendMailAndPushEvent")
    public Executor sendMailAndPushEvent() {
        return new ThreadPoolTaskExecutor();
    }
    @Bean(name = "sendMailAndPushEventInventory")
    public Executor sendMailAndPushEventInventory() {
        return new ThreadPoolTaskExecutor();
    }

}


