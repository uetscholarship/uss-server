package net.bqc.uss.messenger.dao;

import net.bqc.uss.messenger.model.User;

import java.util.List;

public interface UserDao {
	User findByFbId(String fbId);
	List<User> findAll();
	List<User> findAllSubscribedUsers();
	boolean insert(User user);
	void updateSubStatus(String fbId, boolean status);
}
