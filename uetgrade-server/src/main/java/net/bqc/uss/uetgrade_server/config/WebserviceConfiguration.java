package net.bqc.uss.uetgrade_server.config;

import net.bqc.uss.service.UetGradeService;
import net.bqc.uss.uetgrade_server.jaxws.UetGradeServiceEndpoint;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

@Configuration
public class WebserviceConfiguration {

    @Autowired
    private SpringBus bus;

    @Bean
    public UetGradeService uetGradeService() {
        return new UetGradeServiceEndpoint();
    }

    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, uetGradeService());
        endpoint.publish("/UetGrade");
        return endpoint;
    }
}
