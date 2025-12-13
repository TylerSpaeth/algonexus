package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.sql.Timestamp;
import java.util.List;

public class CandlestickDAO {

    /**
     * Get a segment of the Candlesticks that belong to the provided HistoricalDataset.
     * @param historicalDataset HistoricalDataset
     * @param startTime The time to return data after.
     * @param numCandles Maximum number of Candlesticks to return.
     * @return List of Candlesticks.
     */
    public List<Candlestick> getPaginatedCandlesticksFromHistoricalDataset(HistoricalDataset historicalDataset, Timestamp startTime, int numCandles) {
        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Candlestick> cq = cb.createQuery(Candlestick.class);
        Root<Candlestick> root = cq.from(Candlestick.class);

        Predicate predicate = cb.equal(root.get("historicalDataset"), historicalDataset);
        predicate = cb.and(predicate, cb.greaterThan(root.get("timestamp"), startTime));

        cq.select(root).where(predicate).orderBy(cb.asc(root.get("timestamp")));
        return entityManager.createQuery(cq).setMaxResults(numCandles).getResultList();
    }

}
