package net.bqc.uetscholarship.server.config;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.bqc.uetscholarship.service.NotifierService;

/**
 * Created by jcia on 9/28/17.
 */
@Configuration
public class WebserviceConfiguration {

    @Value("${ws.notifier}")
    private String serviceAddress;

    @Bean
    public NotifierService messengerNotifierProxy() {
    	JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    	factory.setServiceClass(NotifierService.class);
    	factory.setAddress(serviceAddress);
    	return (NotifierService) factory.create();
    }
}

