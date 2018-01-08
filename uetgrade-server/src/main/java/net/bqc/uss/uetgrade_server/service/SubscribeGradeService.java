package net.bqc.uss.uetgrade_server.service;

import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import net.bqc.uss.uetgrade_server.repository.CourseRepository;
import net.bqc.uss.uetgrade_server.repository.StudentRepository;
import net.bqc.uss.uetgrade_server.retriever.RetrieveCourseTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class SubscribeGradeService {

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
                String reqCourseCode = course.getCode();

                // handle different names of the course between servers
                if (existedCourse == null) {
                    if (reqCourseCode.matches(".*\\s1$")) { // for case: INT3111 in Database and INT3111 1 for new course
                        reqCourseCode = reqCourseCode.substring(0, reqCourseCode.length() - 2);
                        existedCourse = courseRepository.findByCode(reqCourseCode);
                    }
                    else { // for case: INT3111 1 in Database and INT3111 for new course
                        existedCourse = courseRepository.findByCode(String.format("%s 1", reqCourseCode));
                    }
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
