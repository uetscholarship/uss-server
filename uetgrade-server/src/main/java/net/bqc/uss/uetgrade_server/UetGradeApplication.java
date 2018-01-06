package net.bqc.uss.uetgrade_server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UetGradeApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(UetGradeApplication.class).run(args);
    }
}
