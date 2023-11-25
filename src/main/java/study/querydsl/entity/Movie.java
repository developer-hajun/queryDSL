package study.querydsl.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("M")
public class Movie extends Item{
    private String director;
    private String actor;
}
