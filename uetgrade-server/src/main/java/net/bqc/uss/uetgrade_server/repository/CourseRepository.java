package net.bqc.uss.uetgrade_server.repository;

import net.bqc.uss.uetgrade_server.entity.Course;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface CourseRepository extends CrudRepository<Course, Integer> {

    Course findByCode(String code);

    @Query("select c.code from Course c where c.gradeUrl is not null")
    Set<String> findCodeByGradeUrlNotNull();

    @Query("update Course c set c.gradeUrl=:gradeUrl where c.code=:code")
    @Modifying
    @Transactional(propagation = Propagation.REQUIRED)
    Integer updateGradeUrlByCode(@Param("code") String code, @Param("gradeUrl") String gradeUrl);
}
