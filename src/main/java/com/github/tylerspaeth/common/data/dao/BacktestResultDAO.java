package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.common.data.entity.BacktestResult;
import com.github.tylerspaeth.common.data.entity.BacktestResult_;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet_;
import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class BacktestResultDAO extends AbstractDAO<BacktestResult> {

    /**
     * Get the BacktestResults that match the provided StrategyParameterSet ID.
     * @param strategyParameterSetID ID of a StrategyParameterSet
     * @return List of BacktestResults
     */
    public List<BacktestResult> getBacktestResultsByStrategyParameterSetID(Integer strategyParameterSetID) {
        try (EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager()) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<BacktestResult> cq = cb.createQuery(BacktestResult.class);
            Root<BacktestResult> root = cq.from(BacktestResult.class);

            cq.select(root).where(cb.equal(root.get(BacktestResult_.strategyParameterSet).get(StrategyParameterSet_.strategyParameterSetID), strategyParameterSetID));
            return entityManager.createQuery(cq).getResultList();
        }
    }

}
