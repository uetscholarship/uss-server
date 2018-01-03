package net.bqc.uss.uetgrade_server.retriever;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RetrieveGradeTaskTest {

    @Autowired
    private RetrieveGradeTask retrieveGradeTask;

    @Test
    public void testTaskExecutor() throws IOException {
        retrieveGradeTask.login();
        retrieveGradeTask.getGrades();
    }
}
