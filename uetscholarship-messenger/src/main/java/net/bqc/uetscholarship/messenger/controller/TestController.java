package net.bqc.uetscholarship.messenger.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.bqc.uetscholarship.messenger.dao.UserDao;

@RestController
public class TestController {

	@Autowired
	private UserDao userDao;
	
	@RequestMapping(path = "/getUsers", method = RequestMethod.GET)
	public Object getUsers() {
		return userDao.findAll();
	}
}
