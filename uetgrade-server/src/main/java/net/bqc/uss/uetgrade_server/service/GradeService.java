package net.bqc.uss.uetgrade_server.service;

import net.bqc.uss.uetgrade_server.cache.CacheConstants;
import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import net.bqc.uss.uetgrade_server.repository.CourseRepository;
import net.bqc.uss.uetgrade_server.repository.StudentRepository;
import net.bqc.uss.uetgrade_server.retriever.RetrieveCourseTask;
import net.bqc.uss.uetgrade_server.util.CourseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import java.util.HashSet;
import java.util.Set;

@Service
@CacheDefaults(cacheName = CacheConstants.GRADE_CACHE)
public class GradeService {

    private static final Logger logger = LoggerFactory.getLogger(GradeService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RetrieveCourseTask courseRetriever;

    @CacheResult
    public Student getStudentWithAllCourses(String studentCode) {
        try {
            logger.debug("Get courses for {} from database", studentCode);
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

    /**
     * Evict student in gradeCache by studentCode
     * @param studentCode
     */
    @CacheRemove
    public void evictStudent(String studentCode) {
        // NOTHING TO WRITE HERE
    }

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
                String reqCourseCode = course.getCode().trim();
                reqCourseCode = CourseHelper.normalizeCourseCode(reqCourseCode);
                Course existedCourse = courseRepository.findByCode(reqCourseCode);

                if (existedCourse == null) { // not exists in database
                    // add new course
                    course.setCode(reqCourseCode);
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
