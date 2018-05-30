package net.bqc.uss.uetgrade_server.controller;

import net.bqc.uss.uetgrade_server.entity.Student;
import net.bqc.uss.uetgrade_server.repository.StudentRepository;
import net.bqc.uss.uetgrade_server.service.GradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private GradeService gradeService;

    @Autowired
    private StudentRepository studentRepository;

    @RequestMapping(value = "/evict/{studentCode}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity evict(@PathVariable String studentCode) {
        gradeService.evictStudent(studentCode);
        return new ResponseEntity<>("Done!", HttpStatus.OK);
    }

    @RequestMapping(value = "/getStudent/{studentCode}", method = RequestMethod.GET)
    public ResponseEntity getStudentByCode(@PathVariable String studentCode) {
        logger.debug("Request to get student: {}", studentCode);
        Student s = studentRepository.findByCode(studentCode);
        s.getCourses().stream().forEach(course -> course.setStudents(null));
        logger.debug("Result: {}", s);
        return new ResponseEntity(s, HttpStatus.OK);
    }
}
