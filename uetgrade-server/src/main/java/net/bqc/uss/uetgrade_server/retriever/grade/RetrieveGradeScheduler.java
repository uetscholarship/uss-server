package net.bqc.uss.uetgrade_server.retriever.grade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.bqc.uss.service.MessengerService;
import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import net.bqc.uss.uetgrade_server.repository.CourseRepository;
import net.bqc.uss.uetgrade_server.service.GradeService;
import net.bqc.uss.uetgrade_server.util.CourseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class RetrieveGradeScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RetrieveGradeScheduler.class);

    @Value("${uet.grade.host}")
    private String gradeHost;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RetrieveGradeTask retrieveGradeTask;

    @Autowired
    private MessengerService messengerServiceProxy;

    @Autowired
    private GradeService gradeService;

    private ConcurrentMap<String, Integer> gradedCoursesCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void setupCache() {
        logger.debug("[{}] Setting up cache for scheduler...", Thread.currentThread().getName());
        Set<String> gradedCourseCodes = courseRepository.findCodeByGradeUrlNotNull();
        gradedCourseCodes.forEach(courseCode -> gradedCoursesCache.put(courseCode, 1));
        logger.debug("[{}] Done cache setup!", Thread.currentThread().getName());
    }

    @Async
    @Scheduled(cron = "0 */15 6-19 * * *", zone = "GMT+7")
    //@Scheduled(cron = "0 */1 * * * *", zone = "GMT+7")
    public void retrieveNewGrades() {
        try {
            logger.debug("[{}] Retrieving new graded courses...", Thread.currentThread().getName());
            String rawGrades = retrieveGradeTask.getRawGrades();
            logger.debug("[{}] Done getting raw grades!", Thread.currentThread().getName());

            /**
             * keep below line for the first run, to prepare courses data for database
             * List<Course> newGradedCourses = parse(rawGrades, true);
             */

            // for scheduler, from the second run
            logger.debug("[{}] Parsing raw grades...", Thread.currentThread().getName());
            List<Course> newGradedCourses = parse(rawGrades, false);
            logger.debug("[{}] New graded courses here: {}", Thread.currentThread().getName(), newGradedCourses);

            // cache evicting student codes
            Set<String> evictingStudentCodes = new HashSet<>();

            if (newGradedCourses.size() > 0) {
                // filter, only keep students who are subscribing to get grades
                newGradedCourses.stream()
                        .filter(course -> course.getStudents() != null)
                        .forEach(course -> {
                            Set<Student> filteredStudents = course.getStudents().stream()
                                    .filter(student -> {
                                        if (student.isSubscribed()) {
                                            student.setCourses(null);
                                            evictingStudentCodes.add(student.getCode());
                                        }
                                        return student.isSubscribed();
                                    })
                                    .collect(Collectors.toSet());
                            course.setStudents(filteredStudents);
                        });

                logger.debug("[{}] Evicted student codes: {}", Thread.currentThread().getName(), evictingStudentCodes);
                // evict student whose new grades from grade cache
                evictingStudentCodes.forEach(gradeService::evictStudent);

                logger.debug("[{}] Notifying for Messenger service...", Thread.currentThread().getName());
                // notify new graded course for messenger
                boolean result = messengerServiceProxy.notifyNewGradedCourses(newGradedCourses);
                logger.debug("[{}] Result: {}", Thread.currentThread().getName(), result);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * parse raw grades, get grade url link for course code
     * @Param saveAllCourses Allow to save all the courses to database in spite of un-graded courses
     */
    private List<Course> parse(String rawGrades, boolean saveAllCourses) throws IOException {
        List<Course> newGradedCourses = new ArrayList<>();
        JsonNode termNode = objectMapper.readTree(rawGrades);
        JsonNode coursesNode = termNode.get(0);
        if (coursesNode.isArray()) {
            ArrayNode coursesArray = (ArrayNode) coursesNode;
            for (JsonNode courseNode : coursesArray) {
                String courseCode = courseNode.get(1).asText();
                courseCode = CourseHelper.normalizeCourseCode(courseCode);
                String courseName = courseNode.get(0).asText();
                String gradeUrl = courseNode.get(2).asText();
                gradeUrl = (gradeUrl != null && gradeUrl.trim().isEmpty())
                        ? null : (String.format("%s/%s", gradeHost, gradeUrl));

                if (gradeUrl != null) { // graded courses and not in graded course cache
                    if (!gradedCoursesCache.containsKey(courseCode)) {
                        Course existedCourse = courseRepository.findWithStudentsByCode(courseCode);
                        if (existedCourse != null) {
                            if (existedCourse.getGradeUrl() == null) { // exist db but not graded before
                                courseRepository.updateGradeUrlByCode(courseCode, gradeUrl);
                                // this is the course we have to notify user as a new grade course
                                existedCourse.setGradeUrl(gradeUrl);
                                newGradedCourses.add(existedCourse);
                            }

                            // add course to graded course cache, and do not check after
                            gradedCoursesCache.put(existedCourse.getCode(), 1);
                        }
                        else { // graded course and not exist in db
                            Course newCourse = new Course(courseCode, courseName, gradeUrl);
                            courseRepository.save(newCourse);
                            // course with grade but not exist in db, of course it is a new graded course
                            newGradedCourses.add(newCourse);
                        }
                    }
                    /*else {
                        logger.debug("[{}] Ignore course {}", Thread.currentThread().getName(), courseCode);
                    }*/
                }
                else if (saveAllCourses) { // for first run, save all course include not-graded course
                    Course existedCourse = courseRepository.findByCode(courseCode);
                    if (existedCourse == null) {
                        Course newCourse = new Course(courseCode, courseName, gradeUrl);
                        courseRepository.save(newCourse);
                    }
                }

//                logger.debug("Course information: "
//                        + courseCode + " | "
//                        + courseName + " | "
//                        + gradeUrl);
            }
        }
        return newGradedCourses;
    }
}
