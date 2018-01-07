package net.bqc.uss.messenger.dao;

import net.bqc.uss.messenger.model.Subscriber;
import net.bqc.uss.messenger.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GradeSubscriberDaoImpl {

    private static final Logger logger = LoggerFactory.getLogger(GradeSubscriberDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Subscriber> findAll() {
        String sql = "SELECT * FROM grade_subscribers";
        List<Subscriber> result = jdbcTemplate.query(sql, new SubscriberRowMapper());
        return result;
    }

    public List<String> findSubscribersByStudentCode(String studentCode) {
        try {
            String sql = "SELECT fb_id FROM grade_subscribers WHERE student_code = ?";
            List<String> subscriberIds = jdbcTemplate.queryForList(sql, new Object[] { studentCode }, String.class);
            return subscriberIds;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> findStudentCodesBySubscriber(String fbId) {
        try {
            String sql = "SELECT student_code FROM grade_subscribers WHERE fb_id = ?";
            List<String> studentCodes = jdbcTemplate.queryForList(sql, new Object[] { fbId }, String.class);
            return studentCodes;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean isSubscribed(String fbId, String studentCode) {
        try {
            String sql = "SELECT COUNT(*) FROM grade_subscribers WHERE fb_id = ? AND student_code = ?";
            Integer count = jdbcTemplate.queryForObject(sql, new Object[] { fbId, studentCode }, Integer.class);
            return count > 0;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public boolean insertSubscriber(String fbId, String studentCode) {
        try {
            String sql = "INSERT INTO grade_subscribers (fb_id, student_code) VALUES (? , ?)";
            jdbcTemplate.update(sql, fbId, studentCode);
            return true;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
            return false;
        }
    }

    public class SubscriberRowMapper implements RowMapper<Subscriber> {

        @Override
        public Subscriber mapRow(ResultSet arg0, int arg1) throws SQLException {
            Subscriber subscriber = new Subscriber();
            subscriber.setId(arg0.getInt("id"));
            subscriber.setFbId(arg0.getString("fb_id"));
            subscriber.setStudentCode(arg0.getString("student_code"));
            return subscriber;
        }

    }
}
