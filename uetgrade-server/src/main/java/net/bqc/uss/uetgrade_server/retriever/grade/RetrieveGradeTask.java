package net.bqc.uss.uetgrade_server.retriever.grade;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RetrieveGradeTask {

    private static final Logger logger = LoggerFactory.getLogger(RetrieveGradeTask.class);
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36";

    private static final String LOGIN_PATH = "/submitLoginForm";
    private static final String GET_LIST_SUBJECT_PATH = "/home/getListSubjectOfTerm";

    @Value("${uet.grade.host}")
    private String gradeHost;

    @Value("${uet.grade.term.code}")
    private String termCode;

    @Value("${uet.grade.education.type}")
    private String educationType;

    @Value("${uet.grade.account.username}")
    private String username;

    @Value("${uet.grade.account.password}")
    private String password;

    private Map<String, String> cookies;
    private String token;

    public String getRawGrades() {
        try {
            login();
            String rawGrades = getGrades();
            return rawGrades;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void login() throws IOException {
        // get token to login

        Connection.Response loginPageResponse = Jsoup.connect(gradeHost)
                .userAgent(USER_AGENT)
                .followRedirects(true)
                .execute();

        Document loginPage = loginPageResponse.parse();
        this.cookies = loginPageResponse.cookies();
        this.token = loginPage.select("input[name=_token]")
                .attr("value")
                .trim();

        if (token.isEmpty()) throw new IllegalStateException("Can not get token");

        // prepare parameters map for login form
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("_token", this.token);
        loginParams.put("username", username);
        loginParams.put("password", password);

        // do login
        Jsoup.connect(gradeHost + LOGIN_PATH)
                .userAgent(USER_AGENT)
                .cookies(this.cookies)
                .data(loginParams)
                .method(Connection.Method.POST)
                .followRedirects(true)
                .execute();
    }

    private String getGrades() throws IOException {
        // prepare parameters
        Map<String, String> formParams = new HashMap<>();
        formParams.put("_token", this.token);
        formParams.put("term", termCode);
        formParams.put("type_education", educationType);

        Connection.Response gradesResponse = Jsoup.connect(gradeHost + GET_LIST_SUBJECT_PATH)
                .userAgent(USER_AGENT)
                .cookies(this.cookies)
                .data(formParams)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();

        return gradesResponse.body();
    }
}