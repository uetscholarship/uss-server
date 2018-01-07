package net.bqc.uss.messenger.controller;

import net.bqc.uss.messenger.dao.GradeSubscriberDaoImpl;
import net.bqc.uss.messenger.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@Autowired
	private UserDao userDao;

	@Autowired
	private GradeSubscriberDaoImpl gradeSubscriberDao;
	
	@RequestMapping(path = "/getUsers", method = RequestMethod.GET)
	public Object getUsers() {
		return userDao.findAll();
	}

	@RequestMapping(path = "/getGradeSubscribers", method = RequestMethod.GET)
	public Object getGradeSubscribers() {
		return gradeSubscriberDao.findAll();
	}
}
