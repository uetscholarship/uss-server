package net.bqc.uss.uetgrade_server.repository;

import net.bqc.uss.uetgrade_server.entity.Course;
import org.springframework.data.repository.CrudRepository;

public interface CourseRepository extends CrudRepository<Course, Integer> {

    Course findByCode(String code);
    boolean existsByCode(String code);
}
