package study.querydsl.entity;

import jakarta.persistence.*;

@Entity
public class Perent {
    @Id
    @GeneratedValue
    @Column(name = "perent_id")
    private Long id;

    @Column(name = "USERNAME")
    private String name;
}
