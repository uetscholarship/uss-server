package net.bqc.uss.uetgrade_server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UetGradeApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(UetGradeApplication.class).run(args);
    }
}
