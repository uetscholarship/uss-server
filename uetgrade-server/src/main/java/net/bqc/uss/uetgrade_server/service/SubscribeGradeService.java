package net.bqc.uss.uetgrade_server.service;

import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import net.bqc.uss.uetgrade_server.repository.CourseRepository;
import net.bqc.uss.uetgrade_server.repository.StudentRepository;
import net.bqc.uss.uetgrade_server.retriever.RetrieveCourseTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SubscribeGradeService {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeGradeService.class);

    private static final Pattern p = Pattern.compile("^([A-Z]{3})(\\s)(\\d{4}.*)"); // catch EMA 3048, EMA 3046 2

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RetrieveCourseTask courseRetriever;

    public boolean subscribe(String studentCode) {
        try {
            Student existedStudent = studentRepository.findByCode(studentCode);
            if (existedStudent != null) { // exists in database
                if (!existedStudent.isSubscribed()) { // but un-subscribed before
                    existedStudent.setSubscribed(true);
                    studentRepository.save(existedStudent);
                }
                return true;
            }

            Set<Course> requestMappingCourses = courseRetriever.getCourses(studentCode);
            if (requestMappingCourses.size() == 0) { // impossible student or student enrol no courses
                return false;
            }

            // if course exists in database, so use existed code else use new course
            // add all in finalMappingCourses set
            Set<Course> finalMappingCourses = new HashSet<>();
            Student newStudent = courseRetriever.getStudent();

            for (Course course : requestMappingCourses) {
                Course existedCourse = courseRepository.findByCode(course.getCode());
                String reqCourseCode = course.getCode().trim();

                // handle different names of the course between servers
                if (existedCourse == null) {
                    Matcher m = p.matcher(reqCourseCode);
                    if (m.find()) { // EMA 3084 for new course, but EMA3084 in database
                        logger.debug("Requested code: {}", reqCourseCode);
                        reqCourseCode = m.replaceFirst("$1$3");
                        logger.debug("Normalized code: {}", reqCourseCode);
                        existedCourse = courseRepository.findByCode(reqCourseCode);
                        logger.debug("Course after: {}", existedCourse);
                    }
                }

                if (existedCourse == null) {
                    if (reqCourseCode.matches(".*\\s1$")) { // for case: INT3111 in Database and INT3111 1 for new course
                        reqCourseCode = reqCourseCode.substring(0, reqCourseCode.length() - 2);
                        logger.debug("Normalized code again: {}", reqCourseCode);
                        existedCourse = courseRepository.findByCode(reqCourseCode);
                    }
                    else { // for case: INT3111 1 in Database and INT3111 for new course
                        reqCourseCode = String.format("%s 1", reqCourseCode);
                        logger.debug("Normalized code again: {}", reqCourseCode);
                        existedCourse = courseRepository.findByCode(reqCourseCode);
                    }
                    logger.debug("Course after again: {}", existedCourse);
                }

                if (existedCourse == null) { // not exists in database
                    // add new course
                    Course newCourse = courseRepository.save(course);
                    finalMappingCourses.add(newCourse);
                }
                else finalMappingCourses.add(existedCourse);
            }

            newStudent.setCourses(finalMappingCourses);
            studentRepository.save(newStudent);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String args[]) {
        Pattern p = Pattern.compile("^([A-Z]{3})(\\s)(\\d{4}.*)");
        Matcher m = p.matcher("EMA3084 1".trim());
        if (m.find()) {
            System.out.println(true);
            System.out.println(m.replaceFirst("$1$3"));
        }
    }

    public boolean unsubscribe(String studentCode) {
        try {
            Student student = studentRepository.findByCode(studentCode);
            if (student != null) {
                student.setSubscribed(false);
                studentRepository.save(student);
            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
