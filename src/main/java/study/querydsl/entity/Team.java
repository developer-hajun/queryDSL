package study.querydsl.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of={"id","name"})
public class Team extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    @Column(name="TEAMNAME")
    private String name;

    @OneToMany(mappedBy = "team")
//    @OneToMany
//    @JoinColumn(name = "team_id")
    private List<Member> memberList=new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
