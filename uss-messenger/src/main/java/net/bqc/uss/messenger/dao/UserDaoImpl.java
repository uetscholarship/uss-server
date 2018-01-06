package net.bqc.uss.messenger.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.bqc.uss.messenger.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public User findByFbId(String fbId) {
		try {
			String sql = "SELECT * FROM users WHERE fb_id = ?";
			User user = jdbcTemplate.queryForObject(
					sql, new Object[] { fbId }, new UserRowMapper());
			return user;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<User> findAll() {
		String sql = "SELECT * FROM users";
		List<User> result = jdbcTemplate.query(sql, new UserRowMapper());
		return result;
	}
	
	@Override
	public List<User> findAllSubscribedUsers() {
		String sql = "SELECT * FROM users WHERE is_subscribed IS TRUE";
		List<User> result = jdbcTemplate.query(sql, new UserRowMapper());
		return result;
	}

	@Override
	public boolean insert(User user) {
		try {
			String sql = "INSERT INTO users (fb_id, first_name, last_name) VALUES (?, ?, ?)";
			jdbcTemplate.update(
					sql, user.getFbId(), user.getFirstName(), user.getLastName());
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void updateSubStatus(String fbId, boolean status) {
		try {
			String sql = "UPDATE users SET is_subscribed = ? WHERE fb_id = ?";
			jdbcTemplate.update(sql, status, fbId);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class UserRowMapper implements RowMapper<User> {

		@Override
		public User mapRow(ResultSet arg0, int arg1) throws SQLException {
			User user = new User();
			user.setId(arg0.getInt("id"));
			user.setFbId(arg0.getString("fb_id"));
			user.setFirstName(arg0.getString("first_name"));
			user.setLastName(arg0.getString("last_name"));
			user.setSubscribed(arg0.getBoolean("is_subscribed"));
			return user;
		}
		
	}
}
