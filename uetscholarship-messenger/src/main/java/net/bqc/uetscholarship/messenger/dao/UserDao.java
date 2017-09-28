package net.bqc.uetscholarship.messenger.dao;

import java.util.List;

import net.bqc.uetscholarship.messenger.model.User;

public interface UserDao {
	User findByFbId(String fbId);
	List<User> findAll();
	List<User> findAllSubscribedUsers();
	boolean insert(User user);
	void updateSubStatus(String fbId, boolean status);
}
