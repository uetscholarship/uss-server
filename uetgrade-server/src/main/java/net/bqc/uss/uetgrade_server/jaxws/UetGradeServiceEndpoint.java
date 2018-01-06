package net.bqc.uss.uetgrade_server.jaxws;

import net.bqc.uss.service.UetGradeService;
import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import net.bqc.uss.uetgrade_server.repository.StudentRepository;
import net.bqc.uss.uetgrade_server.service.SubscribeGradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;


@WebService(serviceName = "UetGradeService", portName = "UetGradePort",
        targetNamespace = "http://jaxws.uetgrade_server.uss.bqc.net/",
        endpointInterface = "net.bqc.uss.service.UetGradeService")
public class UetGradeServiceEndpoint extends SpringBeanAutowiringSupport implements UetGradeService {

    private static final Logger logger = LoggerFactory.getLogger(UetGradeServiceEndpoint.class);

    @Autowired
    private SubscribeGradeService subscribeGradeService;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public boolean subscribeGrade(String studentCode) {
        logger.debug("Request subscribe for student: " + studentCode);
        boolean result = subscribeGradeService.subscribe(studentCode);
        logger.debug("SubscribeGradeService return: " + result);
        return result;
    }

    @Override
    public boolean unsubscribeGrade(String studentCode) {
        logger.debug("Request unsubscribe for student: " + studentCode);
        boolean result = subscribeGradeService.unsubscribe(studentCode);
        logger.debug("SubscribeGradeService return: " + result);
        return result;
    }

    @Override
    public Student getStudentWithAllCourses(String studentCode) {
        try {
            logger.debug("Request get graded courses for student: " + studentCode);
            Student student = studentRepository.findByCode(studentCode);
            if (student != null) {
                // set students set null for each courses in order to prevent cyclic problem of JAXB
                student.getCourses().stream().forEach(course -> course.setStudents(null));
                return student;
            }
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
