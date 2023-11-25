package study.querydsl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class AddressEntity {
    @Id
    @GeneratedValue
    @Column(name = "address_ids")
    private Long id;
    private Address address;

    public AddressEntity(String a,String b,String c) {
        this.address = new Address(a,b,c);
    }

    public AddressEntity() {
    }
}
