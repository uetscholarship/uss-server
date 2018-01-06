package net.bqc.uss.uetgrade_server.repository;

import net.bqc.uss.uetgrade_server.entity.Course;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface CourseRepository extends CrudRepository<Course, Integer> {

    Course findByCode(String code);
    boolean existsByCode(String code);

    @Query("update Course c set c.gradeUrl = ?2 where c.code = ?1")
    @Modifying
    @Transactional(propagation = Propagation.REQUIRED)
    Integer updateGradeUrlByCode(String code, String gradeUrl);
}
