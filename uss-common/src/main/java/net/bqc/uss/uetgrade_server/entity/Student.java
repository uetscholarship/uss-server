package net.bqc.uss.uetgrade_server.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "student")
public class Student implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "student_id")
    private Integer id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "class")
    private String clazz;

    @Column(name = "dob")
    private String dob;

    @Column(name = "is_subscribed")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean isSubscribed = true; // default true on the first save to db

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id", insertable = false, nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "course_id", insertable = false, nullable = false, updatable = false)
    )
    private Set<Course> courses = new HashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }

    public void addCourses(Course course) {
        if (this.courses == null) {
            this.courses = new HashSet<>();
        }
        this.courses.add(course);
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Student)) return false;
        else
            return o == this || this.code != null && this.code.equals(((Student) o).code);
    }

    @Override
    public String toString() {
        return "Student{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", isSubscribed=" + isSubscribed +
                ", courses=" + courses +
                '}';
    }
}
