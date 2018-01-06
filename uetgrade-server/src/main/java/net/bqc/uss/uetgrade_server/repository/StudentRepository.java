package net.bqc.uss.uetgrade_server.repository;

import net.bqc.uss.uetgrade_server.entity.Student;
import org.springframework.data.repository.CrudRepository;

public interface StudentRepository extends CrudRepository<Student, Integer> {

    Student findByCode(String code);
    boolean existsByCode(String code);
}
