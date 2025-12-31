package com.github.tylerspaeth.common.data.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "strategies")
public class Strategy {

    @Id
    @Column(name = "StrategyID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer strategyID;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Version")
    private Integer version;

    @Column(name = "Active")
    private Boolean active;

    @Column(name = "CreatedAt")
    private Timestamp createdAt;

    @Column(name = "LastUpdated")
    private Timestamp lastUpdated;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentStrategyID", referencedColumnName = "StrategyID")
    private Strategy parentStrategy;

    @OneToMany(mappedBy="strategy", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<StrategyParameterSet> strategyParameterSets;

    @Override
    public String toString() {
        return name;
    }

    public Integer getStrategyID() {
        return strategyID;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated)  {
        this.lastUpdated = lastUpdated;
    }

    public Strategy getParentStrategy() {
        return parentStrategy;
    }

    public void setParentStrategy(Strategy parentStrategy) {
        this.parentStrategy = parentStrategy;
    }

    public List<StrategyParameterSet> getStrategyParameterSets() {
        if(strategyParameterSets == null) {
            strategyParameterSets = new ArrayList<>();
        }
        return strategyParameterSets;
    }
}
