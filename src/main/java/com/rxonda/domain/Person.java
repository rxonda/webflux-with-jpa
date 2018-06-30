package com.rxonda.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Person {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String address;

    public Person() {}

    public Person(String name, String address) {
        this.name = name;
        this.address = address;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return String.format(
                "Person[id=%d, name='%s', address='%s']",
                id, name, address);
    }
}