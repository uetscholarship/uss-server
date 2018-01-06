package net.bqc.uss.uetgrade_server.entity;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @Column(name = "course_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "course_name")
    private String name;

    @Column(name = "course_code", unique = true)
    private String code;

    @Column(name = "grade_url")
    private String gradeUrl;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students;

    public Course() {
    }

    public Course(String code, String name) {
        this.name = name;
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGradeUrl() {
        return gradeUrl;
    }

    public void setGradeUrl(String gradeUrl) {
        this.gradeUrl = gradeUrl;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Course)) return false;
        else if (o == this) return true;
        else return this.code != null && this.code.equals(((Course)o).code);
    }

    @Override
    public String toString() {
        return "Course{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
