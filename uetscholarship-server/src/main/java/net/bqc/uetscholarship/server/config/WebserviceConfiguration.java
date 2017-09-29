package net.bqc.uetscholarship.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jcia on 9/28/17.
 */
@Configuration
public class WebserviceConfiguration {

    @Value("${messenger.server}")
    private String address;


}
