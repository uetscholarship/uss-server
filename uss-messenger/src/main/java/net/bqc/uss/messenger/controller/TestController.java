package net.bqc.uss.messenger.controller;

import net.bqc.uss.messenger.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@Autowired
	private UserDao userDao;
	
	@RequestMapping(path = "/getUsers", method = RequestMethod.GET)
	public Object getUsers() {
		return userDao.findAll();
	}
}
