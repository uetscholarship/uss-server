package net.bqc.uss.uetgrade_server.config;

import net.bqc.uss.service.MessengerService;
import net.bqc.uss.service.NotifierService;
import net.bqc.uss.service.UetGradeService;
import net.bqc.uss.uetgrade_server.jaxws.UetGradeServiceEndpoint;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

@Configuration
public class WebserviceConfiguration {

    @Value("${ws.notifier}")
    private String messengerServiceAddress;

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

    @Bean
    public MessengerService messengerProxy() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(NotifierService.class);
        factory.setAddress(messengerServiceAddress);
        return (MessengerService) factory.create();
    }
}
