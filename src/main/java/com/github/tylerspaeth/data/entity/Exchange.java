package com.github.tylerspaeth.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "exchanges")
public class Exchange {

    @Id
    @Column(name = "ExchangeID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer exchangeID;

    @Column(name = "Name")
    private String name;

    public Integer getExchangeID() {
        return exchangeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
