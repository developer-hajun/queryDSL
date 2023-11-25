package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.*;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;
import static study.querydsl.entity.QItem.item;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory query;
    @BeforeEach
    public void before() {
        query = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL(){
        Member findByJPQL = em.createQuery("select m from Member m where m.name = :membername ", Member.class)
                .setParameter("membername", "member1").getSingleResult();
        Assertions.assertEquals(findByJPQL.getName(),"member1");
    }

    @Test
    public void startQueryDSL(){
        List<Member> fetch = query
                .select(member)
                .from(member)
                .where(member.age.between(10, 30),member.name.eq("member1")).fetch();
        for (Member fetch1 : fetch) {
            System.out.println(fetch1.getName());
        }
    }

    @Test
    public void resultFetch(){
//        List<Member> fetch = query.selectFrom(member).fetch();
//        Member fetchOne = query.selectFrom(member).fetchOne();
//        Member fetchFirst = query.selectFrom(member).fetchFirst();
//        QueryResults<Member> results = query.selectFrom(member).fetchResults();
//        results.getTotal();
        query.selectFrom(member).fetchCount();
    }

    @Test
    public void Sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));
        List<Member> fetch = query.selectFrom(member).where(member.age.eq(100)).orderBy(member.age.desc(),member.name.asc().nullsLast()).fetch();
        Member member5 = fetch.get(0);
        Member member6 = fetch.get(1);
        Member memberNull = fetch.get(2);

        assertThat(member5.getName()).isEqualTo("member5");
        assertThat(member6.getName()).isEqualTo("member6");
        assertThat(memberNull.getName()).isNull();
    }

    @Test
    public void paging1(){
        List<Member> fetch = query.selectFrom(member).orderBy(member.name.desc()).offset(1).limit(2).fetch();
        assertThat(fetch.size()).isEqualTo(2);
    }

    @Test
    public void paging2(){
        QueryResults<Member> total = query.selectFrom(member).orderBy(member.name.desc()).offset(1).limit(2).fetchResults();
        assertThat(total.getTotal()).isEqualTo(4);
        assertThat(total.getOffset()).isEqualTo(1);
        assertThat(total.getLimit()).isEqualTo(2);
        assertThat(total.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation(){
        List<Tuple> fetch = query.select(
                member.count(), member.age.sum(), member.age.avg(),
                        member.age.max(), member.age.min())
                .from(member).fetch();
        assertThat(fetch.get(0).get(member.count())).isEqualTo(4);
        assertThat(fetch.get(0).get(member.age.sum())).isEqualTo(100);
        assertThat(fetch.get(0).get(member.age.avg())).isEqualTo(25);
        assertThat(fetch.get(0).get(member.age.max())).isEqualTo(40);
        assertThat(fetch.get(0).get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void group() throws Exception{
        //given
        List<Tuple> fetch = query.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        List<Item> fetch1 = query.selectFrom(item).groupBy(item.Price.gt(1000)).fetch();
        //when
        Tuple tupleA = fetch.get(0);
        Tuple tupleB = fetch.get(1);
        //then
        assertThat(tupleA.get(team.name)).isEqualTo("teamA");
        assertThat(tupleA.get(member.age.avg())).isEqualTo(15);
        assertThat(tupleB.get(team.name)).isEqualTo("teamB");
        assertThat(tupleB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    public void join() throws Exception{
        List<Member> fetch = query.
                selectFrom(member).
                innerJoin(member.team, team).
                where(team.name.eq("teamA")).
                fetch();
        //join => Entity만 영속화 , fetchjoin => 연관된 Entity도 함꼐 영속화
        assertThat(fetch).extracting("name").
                containsExactly("member1","member2");
    }

    @Test
    public void theta_join() throws Exception{
        //given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        //when
        List<Member> fetch = query
                .select(member)
                .from(member, team)
                .where(member.name.eq(team.name))
                .fetch();
        //then
        assertThat(fetch)
                .extracting("name")
                .containsExactly("teamA","teamB");
        //외부조인불가능
    }

    @Test
    @Rollback(value = false)
    public void join_on_filtering() {
        //given
        List<Tuple> teamA = query.select(member, team).from(member).leftJoin(member.team, team).on(team.name.eq("teamA")).fetch();
        //when
        //then
        for (Tuple tuple : teamA) {
            System.out.println(tuple.get(member));
            System.out.println(tuple.get(team));
        }
    }
    @Autowired
    EntityManagerFactory emf;
    @Test
    public void NoFetchJoin() throws Exception{
        //given
        em.flush();em.clear();
        Member member1 = query.selectFrom(member).where(member.name.eq("member1")).fetchOne();
        //when
        Team team1 = member1.getTeam();
        System.out.println(team1);
//        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
//        assertThat(loaded).as("패치조인 미적용").isFalse();
        //then
    }
    @Test
    public void FetchJoin() throws Exception{
        //given
        em.flush();em.clear();
        Member member1 = query
                .selectFrom(member)
                .where(member.name.eq("member1"))
                .join(member.team, team).fetchJoin()
                .fetchOne();
        Team team1 = member1.getTeam();
        System.out.println(team1);
//        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
//        assertThat(loaded).as("패치조인 미적용").isTrue();
    }
    @Test
    public void subQuery() throws Exception{
        //given
        QMember memberSub = new QMember("memberSub");
        List<Member> fetch = query
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max()).
                                from(memberSub)))
                .fetch();
        assertThat(fetch).extracting("age").containsExactly(40);
        //when
        //then
    }
    @Test
    public void Goe() throws Exception{
        //given
        QMember memberSub = new QMember("memberSub");
        List<Member> fetch = query.selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                )).fetch();
        //when
        assertThat(fetch).extracting("age").containsExactly(30,40);
        //then
    }
    @Test
    public void in() throws Exception{
        QMember memberSub = new QMember("memberSub");
        List<Member> fetch = query.selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                )).fetch();
        assertThat(fetch).extracting("age").containsExactly(20,30,40);
    }
    @Test
    public void selectSubQuery() throws Exception{
        //given
        QMember memberSub = new QMember("memberSub");

        List<Tuple> fetch = query.
                select(member.name,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();
        //when
        for (Tuple tuple : fetch) {
            System.out.println(tuple);
        }
        //then
    }
    @Test
    public void baseCase() throws Exception{
        //given
        List<String> fetch = query.select(member.age.
                when(10).then("열살").
                when(20).then("스무살").
                otherwise("기타")).
                from(member).
                fetch();//다른값으로 대체
        //when
        for (String s : fetch) {
            System.out.println(s);
        }
        //then
    }
    @Test
    public void ComplexCase() throws Exception{
        //given
        List<String> 기타 = query.select(new CaseBuilder()
                        .when(member.age.between(10, 20)).then("10~20")
                        .when(member.age.between(21, 40)).then("21~40")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();
        //when
        for (String s : 기타) {
            System.out.println(s);
        }
        //then
    }
}