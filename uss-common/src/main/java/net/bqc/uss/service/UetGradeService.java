package net.bqc.uss.service;

import javax.jws.WebService;

@WebService(name = "UetGrade", targetNamespace = "http://jaxws.uetgrade_server.uss.bqc.net/")
public interface UetGradeService {

    void subscribeGrade(String studentCode);
    void unsubscribeGrade(String studentCode);
}
