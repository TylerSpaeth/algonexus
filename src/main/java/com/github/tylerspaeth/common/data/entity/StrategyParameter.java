package com.github.tylerspaeth.common.data.entity;

import com.github.tylerspaeth.common.data.dao.StrategyParameterDAO;
import jakarta.persistence.*;

@Entity
@Table(name = "strategyparameters")
public class StrategyParameter {

    private static final StrategyParameterDAO strategyParameterDAO = new StrategyParameterDAO();

    @Id
    @Column(name = "StrategyParameterID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer strategyParameterID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StrategyParameterSetID", referencedColumnName = "StrategyParameterSetID", nullable = false)
    private StrategyParameterSet strategyParameterSet;

    @Column(name = "Name")
    private String name;

    @Column(name = "Value")
    private String value;

    @Override
    public String toString() {
        return name + " : " + value;
    }

    public Integer getStrategyParameterID() {
        return strategyParameterID;
    }

    public StrategyParameterSet getStrategyParameterSet() {
        strategyParameterSet = strategyParameterDAO.lazyLoad(this, e -> e.strategyParameterSet);
        return strategyParameterSet;
    }

    public void setStrategyParameterSet(StrategyParameterSet strategyParameterSet) {
        this.strategyParameterSet = strategyParameterSet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
