package net.bqc.uss.uetgrade_server.retriever.grade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.bqc.uss.service.MessengerService;
import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import net.bqc.uss.uetgrade_server.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Scheduled(cron = "0 */30 6-18 * * MON-FRI", zone = "GMT+7")
//	@Scheduled(cron = "0 */1 * * * *", zone = "GMT+7")
    public void retrieveNewGrades() {
        try {
            logger.debug("Retrieving new graded courses...");
            String rawGrades = retrieveGradeTask.getRawGrades();

            /**
             * keep below line for the first run, to prepare courses data for database
             * List<Course> newGradedCourses = parse(rawGrades, true);
             */

            // for scheduler, from the second run
            List<Course> newGradedCourses = parse(rawGrades, false);
            logger.debug("New graded courses here: " + newGradedCourses);

            if (newGradedCourses.size() > 0) {
                // filter, only keep students who are subscribing to get grades
                newGradedCourses.forEach(course -> {
                    Set<Student> filteredStudents = course.getStudents().stream()
                            .filter(student -> {
                                student.setCourses(null);
                                return student.isSubscribed();
                            })
                            .collect(Collectors.toSet());
                    course.setStudents(filteredStudents);
                });

                logger.debug("Notifying for Messenger service...");
                // notify new graded course for messenger
                boolean result = messengerServiceProxy.notifyNewGradedCourses(newGradedCourses);
                logger.debug("Result: {}", result);

            }
        }
        catch (IOException e) {
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
                String courseName = courseNode.get(0).asText();
                String gradeUrl = courseNode.get(2).asText();
                gradeUrl = (gradeUrl != null && gradeUrl.trim().isEmpty())
                        ? null : (String.format("%s/%s", gradeHost, gradeUrl));

                if (gradeUrl != null) { // graded courses
                    Course existedCourse = courseRepository.findByCode(courseCode);
                    if (existedCourse != null) {
                        if (existedCourse.getGradeUrl() == null) { // exist db but not graded before
                            courseRepository.updateGradeUrlByCode(courseCode, gradeUrl);
                            // this is the course we have to notify user as a new grade course
                            existedCourse.setGradeUrl(gradeUrl);
                            newGradedCourses.add(existedCourse);
                        }
                    }
                    else { // graded course and not exist in db
                        Course newCourse = new Course(courseCode, courseName, gradeUrl);
                        courseRepository.save(newCourse);
                        // course with grade but not exist in db, of course it is a new graded course
                        newGradedCourses.add(newCourse);
                    }
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
