package com.github.tylerspaeth.common.data.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "strategyparametersets")
public class StrategyParameterSet {

    @Id
    @Column(name = "StrategyParameterSetID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer strategyParameterSetID;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StrategyID", referencedColumnName = "StrategyID", nullable = false)
    private Strategy strategy;

    @OneToMany(mappedBy = "strategyParameterSet", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<StrategyParameter> strategyParameters;

    @Override
    public String toString() {
        return name;
    }

    public Integer getStrategyParameterSetID() {
        return strategyParameterSetID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StrategyParameter> getStrategyParameters() {
        if(strategyParameters == null) {
            strategyParameters = new ArrayList<>();
        }
        return strategyParameters;
    }

}
