package net.bqc.uss.uetgrade_server.repository;

import net.bqc.uss.uetgrade_server.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends CrudRepository<Student, Integer> {

    @Query("select s from Student s left join fetch s.courses where s.code=:code")
    Student findWithCoursesByCode(@Param("code") String code);
    Student findByCode(String code);
    boolean existsByCode(String code);
}
