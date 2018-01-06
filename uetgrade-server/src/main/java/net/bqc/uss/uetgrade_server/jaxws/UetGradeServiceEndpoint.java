package net.bqc.uss.uetgrade_server.jaxws;

import net.bqc.uss.service.UetGradeService;
import net.bqc.uss.uetgrade_server.service.SubscribeGradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;


@WebService(serviceName = "UetGradeService", portName = "UetGradePort",
        targetNamespace = "http://jaxws.uetgrade_server.uss.bqc.net/",
        endpointInterface = "net.bqc.uss.service.UetGradeService")
public class UetGradeServiceEndpoint extends SpringBeanAutowiringSupport implements UetGradeService {

    private static final Logger logger = LoggerFactory.getLogger(UetGradeServiceEndpoint.class);

    @Autowired
    private SubscribeGradeService subscribeGradeService;

    public void subscribeGrade(String studentCode) {
        logger.debug("Request subscribe for student: " + studentCode);
        boolean result = subscribeGradeService.subscribe(studentCode);
        logger.debug("SubscribeGradeService return: " + result);
    }

    public void unsubscribeGrade(String studentCode) {
        logger.debug("Request unsubscribe for student: " + studentCode);
        boolean result = subscribeGradeService.unsubscribe(studentCode);
        logger.debug("SubscribeGradeService return: " + result);
    }
}
