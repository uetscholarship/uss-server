package net.bqc.uss.service;

import net.bqc.uss.uetgrade_server.entity.Student;

import javax.jws.WebService;

@WebService(name = "UetGrade", targetNamespace = "http://jaxws.uetgrade_server.uss.bqc.net/")
public interface UetGradeService {

    boolean subscribeGrade(String studentCode);
    boolean unsubscribeGrade(String studentCode);
    Student getStudentWithGradedCourse(String studentCode);
}
