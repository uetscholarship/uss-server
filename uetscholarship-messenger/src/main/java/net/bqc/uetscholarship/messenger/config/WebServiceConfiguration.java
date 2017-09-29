package net.bqc.uetscholarship.messenger.config;

import net.bqc.uetscholarship.messenger.jaxws.NotifierServiceEndpoint;
import net.bqc.uetscholarship.service.NotifierService;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

@Configuration
public class WebServiceConfiguration {

    @Autowired
    private SpringBus bus;

    @Bean
    public NotifierService notifierService() {
    	return new NotifierServiceEndpoint();
    }
    
    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, notifierService());
        endpoint.publish("/Notifier");
        return endpoint;
    }
}
