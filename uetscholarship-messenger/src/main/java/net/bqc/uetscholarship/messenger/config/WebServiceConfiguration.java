package net.bqc.uetscholarship.messenger.config;

import net.bqc.uetscholarship.messenger.jaxws.NotifierServiceEndpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

@Configuration
public class WebServiceConfiguration {

    @Autowired
    private Bus bus;

    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, new NotifierServiceEndpoint());
        endpoint.publish("/Notifier");
        return endpoint;
    }
}
