package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;

public abstract class AbstractDAO<T> {

    /**
     * Persists a new object to the database.
     * @param t Object to persist.
     */
    public void insert(T t) {
        EntityManager em = DatasourceConfig.entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(t);
        em.getTransaction().commit();
        em.close();
    }

    /**
     * Persists a new object or updates an existing object in the database.
     * @param t Object to persist.
     * @return Most recent version of this object.
     */
    public T update(T t) {
        EntityManager em = DatasourceConfig.entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        T managed = em.merge(t);
        em.getTransaction().commit();
        em.close();
        return managed;
    }

}
