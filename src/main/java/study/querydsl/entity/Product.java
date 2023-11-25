package study.querydsl.entity;

import jakarta.persistence.*;

@Entity
public class Product {
    @Id
    @Column(name = "product_id")
    @GeneratedValue
    private Long id;


}
