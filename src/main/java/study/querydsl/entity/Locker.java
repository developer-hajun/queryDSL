package study.querydsl.entity;

import jakarta.persistence.*;

@Entity
public class Locker {
    @Id
    @GeneratedValue
    @Column(name = "locker_id")
    private Long id;
    private String name;

    @OneToOne(mappedBy = "locker")
    private Member member;
}
