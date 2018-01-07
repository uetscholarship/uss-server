package net.bqc.uss.messenger.jaxws;

import com.restfb.types.send.Message;
import net.bqc.uss.messenger.dao.GradeSubscriberDaoImpl;
import net.bqc.uss.messenger.service.MyMessengerService;
import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;
import java.util.*;
import java.util.stream.Collectors;

@WebService(serviceName = "MessengerService", portName = "MessengerPort",
		targetNamespace = "http://jaxws.messenger.uss.bqc.net/",
		endpointInterface = "net.bqc.uss.service.MessengerService")
public class MessengerServiceEndpoint extends SpringBeanAutowiringSupport implements net.bqc.uss.service.MessengerService {

	private static final Logger logger = LoggerFactory.getLogger(MessengerServiceEndpoint.class);

	@Autowired
	private MyMessengerService myMessengerService;

	@Autowired
	private GradeSubscriberDaoImpl gradeSubscriberDao;

	public boolean notifyNewGradedCourses(List<Course> courses) {
		try {
			if (courses == null) return false;
			List<Student> students = new ArrayList<>();

			logger.debug("Receive new graded courses: {}", courses);
			courses.stream()
				.filter(course -> course.getGradeUrl() != null && course.getStudents() != null)
				.forEach(course -> course.getStudents().forEach(studentInResp -> {
						Student student;
						int pos = students.indexOf(studentInResp);
						if (pos < 0) {
							student = studentInResp;
							students.add(student);
						}
						else student = students.get(pos);
						student.addCourses(course);
					}));

			if (students.size() == 0) {
			    return true;
            }

			logger.debug("Sending grades for student codes: {}", students.stream()
					.map(Student::getCode).collect(Collectors.toList()));
			students.stream()
				.filter(Student::isSubscribed)
				.forEach(student -> {
					List<String> subscribers = gradeSubscriberDao.findSubscribersByStudentCode(student.getCode());
					if (student.getCourses().size() > 0) {
						subscribers.forEach(subscriber -> {
							Message introMessage = myMessengerService.buildGenericMessage(
									myMessengerService.getMessage("grade.new_grade.std.title", null),
									myMessengerService.getMessage("grade.new_grade.std.subtitle",
											new Object[] { student.getName(), student.getCode(), student.getCourses().size() }),
									null , null
							);
							Message gradesInfoMessage = myMessengerService.buildCoursesInfoMessage(student.getCourses());
							myMessengerService.sendMessage(subscriber, introMessage);
							myMessengerService.sendMessage(subscriber, gradesInfoMessage);
							logger.debug("Sent grades of student code [{}] for subscriber [{}]", student.getCode(), subscriber);
						});
					}
				});

			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
