package net.bqc.uss.uetgrade_server.retriever;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RetrieveCourseTaskTest {

    @Autowired
    private RetrieveCourseTask retrieveCourseTask;

    @Test
    public void testTaskExecutor() {
        retrieveCourseTask.getCourses("14020001");
    }
}
