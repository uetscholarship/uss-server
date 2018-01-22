package net.bqc.uss.uetgrade_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfiguration {

    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(5);
    }
}
