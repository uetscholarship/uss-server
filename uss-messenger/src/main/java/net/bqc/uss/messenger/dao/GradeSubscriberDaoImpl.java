package net.bqc.uss.messenger.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GradeSubscriberDaoImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> findSubscribersByStudentCode(String studentCode) {
        try {
            String sql = "SELECT fb_id FROM grade_subscribers WHERE student_code = ?";
            List<String> subscriberIds = jdbcTemplate.queryForList(sql, new Object[] { studentCode }, String.class);
            return subscriberIds;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> findStudentCodesBySubscriber(String fbId) {
        try {
            String sql = "SELECT student_code FROM grade_subscribers WHERE fb_id = ?";
            List<String> studentCodes = jdbcTemplate.queryForList(sql, new Object[] { fbId }, String.class);
            return studentCodes;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean insertSubscriber(String fbId, String studentCode) {
        try {
            String sql = "INSERT INTO grade_subscribers (fb_id, student_code) VALUES (? , ?)";
            jdbcTemplate.update(sql, fbId, studentCode);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSubscriber(String fbId, String studentCode) {
        try {
            String sql = "DELETE FROM grade_subscribers WHERE fb_id = ? AND student_code = ?";
            jdbcTemplate.update(sql, fbId, studentCode);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
