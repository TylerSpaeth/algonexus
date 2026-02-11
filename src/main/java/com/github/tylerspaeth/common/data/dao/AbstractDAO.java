package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;

import java.util.function.Function;

public abstract class AbstractDAO<T> {

    /**
     * Persists a new object to the database.
     * @param t Object to persist.
     */
    public void insert(T t) {
        try (EntityManager em = DatasourceConfig.entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(t);
            em.getTransaction().commit();
        }
    }

    /**
     * Persists a new object or updates an existing object in the database.
     * @param t Object to persist.
     * @return Most recent version of this object.
     */
    public T update(T t) {
        try (EntityManager em = DatasourceConfig.entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            T managed = em.merge(t);
            em.getTransaction().commit();
            return managed;
        }
    }

    /**
     * Lazy loads the provided fields.
     * @param entity Entity
     * @param association Getter for field that need to be loaded.
     */
    public <R> R lazyLoad(T entity, Function<T, R> association) {

        R result = association.apply(entity);

        if(!Hibernate.isInitialized(result)) {
            try (EntityManager em = DatasourceConfig.entityManagerFactory.createEntityManager()) {
                T attached = em.merge(entity);
                result = association.apply(attached);
                Hibernate.initialize(result);
            }
        }
        return result;
    }

}
