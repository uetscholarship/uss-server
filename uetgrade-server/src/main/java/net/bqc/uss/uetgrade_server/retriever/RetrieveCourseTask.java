package net.bqc.uss.uetgrade_server.retriever;

import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
@RequestScope
public class RetrieveCourseTask {

    @Value("${uet.course.api}")
    private String COURSE_API;

    private static final Logger logger = LoggerFactory.getLogger(RetrieveCourseTask.class);
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36";

    private Student student;
    private Set<Course> courses;

    @PostConstruct
    public void init() {
        this.student = new Student();
        this.courses = new HashSet<>();
    }

    public Set<Course> getCourses(String studentCode) {
        try {
            this.student.setCode(studentCode);
            logger.debug("Sending request for student code: " + studentCode);
            Document document = getRaw(studentCode);
            logger.debug("Parsing response:");
            return parse(document);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document getRaw(String studentCode) throws IOException {
        Connection.Response response = Jsoup.connect(COURSE_API)
                .userAgent(USER_AGENT)
                .data("keysearch", studentCode)
                .method(Connection.Method.POST)
                .validateTLSCertificates(false)
                .followRedirects(true)
                .execute();
        return response.parse();
    }

    public Set<Course> parse(Document responseDoc) {
        Element tableBody = responseDoc.select("tbody").get(0);
        // empty result
        if (tableBody.childNodeSize() == 0 || tableBody.child(0).childNodeSize() <= 1)
            return this.courses;

        Element studentNameCell = tableBody.child(0).child(2);
        String studentName = studentNameCell.text().trim();
        this.student.setName(studentName);
        logger.debug("Student name: " + studentName);

        for (Element row : tableBody.children()) { // tr
            Element courseCodeCell = row.child(6);
            String courseCode = courseCodeCell.text().trim();
            if (!courseCode.isEmpty()) {
                Element courseNameCell = row.child(7);
                String courseName = courseNameCell.text().trim();
                Course course = new Course(courseCode, courseName);
                this.courses.add(course);
                logger.debug("Retrieved course: " + course);
            }
        }
        return this.courses;
    }

    public Student getStudent() {
        return student;
    }
}
