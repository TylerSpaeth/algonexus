package com.github.tylerspaeth.common.data.entity;

import com.github.tylerspaeth.common.data.dao.ParameterOptimizationDAO;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "parameteroptimization")
public class ParameterOptimization {

    private static final ParameterOptimizationDAO parameterOptimizationDAO = new ParameterOptimizationDAO();

    @Id
    @Column(name = "ParameterOptimizationID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer parameterOptimizationID;

    @Column(name = "StartTime")
    private Timestamp startTime;

    @Column(name = "EndTime")
    private Timestamp endTime;

    @OneToMany(mappedBy = "parameterOptimization", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<BacktestResult> backtestResults;

    public Integer getParameterOptimizationID() {
        return parameterOptimizationID;
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

    public List<BacktestResult> getBacktestResults() {
        backtestResults = parameterOptimizationDAO.lazyLoad(this, e -> e.backtestResults);
        return backtestResults;
    }

    public void setBacktestResult(List<BacktestResult> backtestResults) {
        this.backtestResults = backtestResults;
    }
}
