package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.config.DatasourceConfig;
import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class HistoricalDatasetDAO extends AbstractDAO<HistoricalDataset> {

    /**
     * Gets all the HistoricalDatasets that exist.
     * @return List of HistoricalDatasets
     */
    public List<HistoricalDataset> getAllHistoricalDatasets() {
        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HistoricalDataset> cq = cb.createQuery(HistoricalDataset.class);
        Root<HistoricalDataset> root = cq.from(HistoricalDataset.class);
        cq.select(root);
        return entityManager.createQuery(cq).getResultList();
    }

}
