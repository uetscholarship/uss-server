package net.bqc.uss.uetgrade_server.retriever;

import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.repository.CourseRepository;
import net.bqc.uss.uetgrade_server.retriever.grade.RetrieveGradeScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RetrieveGradeSchedulerTest {

    @Autowired
    private RetrieveGradeScheduler retrieveGradeScheduler;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    public void testScheduler() {
        retrieveGradeScheduler.retrieveNewGrades();
    }

    @Test
    public void testInsertCourse() {
        Course course = new Course("INT2212", "Công nghệ phần mềm");
//        course = new Course("INT2212", "C\u00F4ng ngh\u1EC7 ph\u1EA7n m\u1EC1m");
        System.out.println(course);
        courseRepository.save(course);
    }
}
