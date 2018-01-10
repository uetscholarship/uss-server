package net.bqc.uss.uetgrade_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class SchedulingConfiguration {

    @Bean
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(2);
    }
}
