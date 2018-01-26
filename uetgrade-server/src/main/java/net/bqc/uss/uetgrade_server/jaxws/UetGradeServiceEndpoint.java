package net.bqc.uss.uetgrade_server.jaxws;

import net.bqc.uss.service.UetGradeService;
import net.bqc.uss.uetgrade_server.entity.Student;
import net.bqc.uss.uetgrade_server.service.GradeService;
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
    private GradeService gradeService;

    @Override
    public boolean subscribeGrade(String studentCode) {
        logger.debug("Request subscribe for student: " + studentCode);
        boolean result = gradeService.subscribe(studentCode);
        logger.debug("SubscribeGradeService return: " + result);
        return result;
    }

    @Override
    public boolean unsubscribeGrade(String studentCode) {
        logger.debug("Request unsubscribe for student: " + studentCode);
        boolean result = gradeService.unsubscribe(studentCode);
        logger.debug("SubscribeGradeService return: " + result);
        return result;
    }

    @Override
    public Student getStudentWithAllCourses(String studentCode) {
        logger.debug("Request get graded courses for student: " + studentCode);
        Student student = gradeService.getStudentWithAllCourses(studentCode);
        logger.debug("Result: {}", student);
        return student;
    }
}

