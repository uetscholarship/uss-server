package net.bqc.uss.service;

import net.bqc.uss.uetgrade_server.entity.Course;

import javax.jws.WebService;
import java.util.List;

@WebService(name = "Messenger", targetNamespace = "http://jaxws.messenger.uss.bqc.net/")
public interface MessengerService {

    boolean notifyNewGradedCourses(List<Course> courses);
}
