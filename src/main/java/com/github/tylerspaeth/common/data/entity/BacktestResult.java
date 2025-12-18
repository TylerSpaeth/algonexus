package com.github.tylerspaeth.common.data.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "backtestresults")
public class BacktestResult {

    @Id
    @Column(name = "BacktestResultID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer backtestResultID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StrategyParameterSetID", referencedColumnName = "StrategyParameterSetID", nullable = false)
    private StrategyParameterSet strategyParameterSet;

    @Column(name = "StartTime")
    private Timestamp startTime;

    @Column(name = "EndTime")
    private Timestamp endTime;

    public Integer getBacktestResultID() {
        return backtestResultID;
    }

    public StrategyParameterSet getStrategyParameterSet() {
        return strategyParameterSet;
    }

    public void setStrategyParameterSet(StrategyParameterSet strategyParameterSet) {
        this.strategyParameterSet = strategyParameterSet;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}
