package net.bqc.uss.uetgrade_server.retriever;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RetrieveGradeTask {

    @Value("${uet.grade.host}")
    private String GRADE_HOST;

    @Value("${uet.grade.account.username}")
    private String GRADE_ACCOUNT_USERNAME;

    @Value("${uet.grade.account.password}")
    private String GRADE_ACCOUNT_PASSWORD;
}
