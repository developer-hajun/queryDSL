package study.querydsl.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity{
    public Member(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id",unique = true)
    private Locker locker;

    @ManyToMany
    @JoinTable(name = "member_product")
    private List<Product> products = new ArrayList<>();

    @Embedded
    private Address address;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city",
                    column = @Column(name = "second_city")),
            @AttributeOverride(name = "street",
                    column = @Column(name = "second_street")),
            @AttributeOverride(name = "zipcode",
                    column = @Column(name = "second_zipcode"))
    })
    private Address address2;

    @Embedded
    private Period period;

    @ElementCollection
    @CollectionTable(name = "favorite_food",
    joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "food_name")
    private Set<String> Foods = new HashSet<>();

//    @ElementCollection
//    @CollectionTable(name = "address",joinColumns = @JoinColumn(name = "member_id"))
//    private List<Address> addresseHistory = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "member_id")
    private List<AddressEntity> addressEntity = new ArrayList<>();

    public Member(String name, int age, Team team) {
        this.name = name;
        this.age = age;
        this.team = team;
    }

    public Member(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void ChangeTeam(Team team){
        this.team = team;
        team.getMemberList().add(this);
    }
}
