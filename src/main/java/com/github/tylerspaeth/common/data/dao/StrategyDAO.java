package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.common.data.entity.Strategy_;
import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class StrategyDAO extends AbstractDAO<Strategy> {

    /**
     * Gets a list of all strategies that are marked as active.
     * @return List of Strategy
     */
    public List<Strategy> getAllActiveStrategies() {
        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Strategy> cq = cb.createQuery(Strategy.class);
        Root<Strategy> root = cq.from(Strategy.class);

        cq.select(root).where(cb.isTrue(root.get(Strategy_.active)));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Gets a list of  all  strategies
     * @return List of Strategy
     */
    public List<Strategy> getAllStrategies() {
        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Strategy> cq = cb.createQuery(Strategy.class);
        Root<Strategy> root = cq.from(Strategy.class);

        cq.select(root);
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Gets a list of all strategies with the provided name.
     * @param name String
     * @return List of Strategy objects order by version ascending.
     */
    public List<Strategy> getStrategiesByName(String name) {
        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Strategy> cq = cb.createQuery(Strategy.class);
        Root<Strategy> root = cq.from(Strategy.class);

        cq.select(root).where(cb.equal(root.get(Strategy_.name), name)).orderBy(cb.asc(root.get(Strategy_.version)));

        return entityManager.createQuery(cq).getResultList();
    }
}
