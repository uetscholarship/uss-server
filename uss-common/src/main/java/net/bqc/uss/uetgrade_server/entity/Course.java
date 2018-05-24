package net.bqc.uss.uetgrade_server.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "course")
public class Course implements Serializable {

    @Id
    @Column(name = "course_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "grade_url")
    private String gradeUrl;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.EAGER)
    private Set<Student> students;

    @Column(name = "num_credit")
    private Integer numCredit;

    public Course() {
    }

    public Course(String code, String name) {
        this.name = name;
        this.code = code;
    }

    public Course(String code, String name, String gradeUrl) {
        this.name = name;
        this.code = code;
        this.gradeUrl = gradeUrl;
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

    public Integer getNumCredit() {
        return numCredit;
    }

    public void setNumCredit(Integer numCredit) {
        this.numCredit = numCredit;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Course)) return false;
        else
            return o == this || this.code != null && this.code.equals(((Course) o).code);
    }

    @Override
    public String toString() {
        return "Course{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", gradeUrl='" + gradeUrl + '\'' +
                '}';
    }
}
