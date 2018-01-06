package net.bqc.uss.messenger.config;

import net.bqc.uss.messenger.jaxws.MessengerServiceEndpoint;
import net.bqc.uss.messenger.jaxws.NotifierServiceEndpoint;
import net.bqc.uss.service.MessengerService;
import net.bqc.uss.service.NotifierService;

import net.bqc.uss.service.UetGradeService;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

@Configuration
public class WebServiceConfiguration {

    @Value("${ws.grade}")
    private String uetGradeServiceAddress;

    @Autowired
    private SpringBus bus;

    @Bean
    public NotifierService notifierService() {
    	return new NotifierServiceEndpoint();
    }

    @Bean
    public MessengerService messengerService() {
        return new MessengerServiceEndpoint();
    }
    
    @Bean
    public Endpoint endpointNotifier() {
        EndpointImpl endpoint = new EndpointImpl(bus, notifierService());
        endpoint.publish("/Notifier");
        return endpoint;
    }

    @Bean
    public Endpoint endpointMessenger() {
        EndpointImpl endpoint = new EndpointImpl(bus, messengerService());
        endpoint.publish("/Messenger");
        return endpoint;
    }


    @Bean
    public UetGradeService uetGradeProxy() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(NotifierService.class);
        factory.setAddress(uetGradeServiceAddress);
        return (UetGradeService) factory.create();
    }
}
