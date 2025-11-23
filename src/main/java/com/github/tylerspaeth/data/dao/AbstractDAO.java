package com.github.tylerspaeth.data.dao;

import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;

public abstract class AbstractDAO<T> {

    /**
     * Persists an object to the database
     * @param t The object to persist
     */
    public void save(T t) {
        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(t);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

}
