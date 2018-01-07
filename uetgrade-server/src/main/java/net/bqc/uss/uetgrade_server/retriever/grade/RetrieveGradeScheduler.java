package net.bqc.uss.uetgrade_server.retriever.grade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.bqc.uss.service.MessengerService;
import net.bqc.uss.uetgrade_server.entity.Course;
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
//	@Scheduled(cron = "*/30 * * * * *", zone = "GMT+7")
    public void retrieveNewGrades() {
        try {
            logger.debug("Retrieving new graded courses...");
            String rawGrades = retrieveGradeTask.getRawGrades();
            List<Course> newGradedCourses = parse(rawGrades, true);
            logger.debug("New graded courses here: " + newGradedCourses);

            logger.debug("Notifying for Messenger service...");
            // TODO: notify new graded course for messenger

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
                            newGradedCourses.add(existedCourse);
                        }
                    }
                    else { // graded course and not exist in db
                        Course newCourse = new Course(courseCode, courseName, gradeUrl);
                        courseRepository.save(newCourse);
                    }
                }
                else if (saveAllCourses) { // for first run, save all course include not-graded course
                    Course existedCourse = courseRepository.findByCode(courseCode);
                    if (existedCourse == null) {
                        Course newCourse = new Course(courseCode, courseName, gradeUrl);
                        courseRepository.save(newCourse);
                    }
                }

                logger.debug("Course information: "
                        + courseCode + " | "
                        + courseName + " | "
                        + gradeUrl);
            }
        }
        return newGradedCourses;
    }
}
