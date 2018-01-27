package net.bqc.uss.uetgrade_server.controller;

import net.bqc.uss.uetgrade_server.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @Autowired
    private GradeService gradeService;

    @RequestMapping(value = "/evict/{studentCode}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity evict(@PathVariable String studentCode) {
        gradeService.evictStudent(studentCode);
        return new ResponseEntity<>("Done!", HttpStatus.OK);
    }
}
