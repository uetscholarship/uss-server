package net.bqc.uss.uetgrade_server.jaxws;

import net.bqc.uss.service.UetGradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;


@WebService(serviceName = "UetGradeService", portName = "UetGradePort",
        targetNamespace = "http://jaxws.uetgrade_server.uss.bqc.net/",
        endpointInterface = "net.bqc.uss.service.UetGradeService")
public class UetGradeServiceEndpoint extends SpringBeanAutowiringSupport implements UetGradeService {

    private static final Logger logger = LoggerFactory.getLogger(UetGradeServiceEndpoint.class);
}
