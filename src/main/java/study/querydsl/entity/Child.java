package study.querydsl.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Child {
    @Id
    @GeneratedValue
    @Column(name = "child_id")
    private Long id;

    @Column(name = "USERNAME")
    private String name;
}
