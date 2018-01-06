package net.bqc.uss.messenger.jaxws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

@WebService(serviceName = "MessengerService", portName = "MessengerPort",
		targetNamespace = "http://jaxws.messenger.uss.bqc.net/",
		endpointInterface = "net.bqc.uss.service.MessengerService")
public class MessengerServiceEndpoint extends SpringBeanAutowiringSupport implements net.bqc.uss.service.MessengerService {

	private static final Logger logger = LoggerFactory.getLogger(MessengerServiceEndpoint.class);

}
