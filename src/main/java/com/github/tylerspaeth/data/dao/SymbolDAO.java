package com.github.tylerspaeth.data.dao;

import com.github.tylerspaeth.config.DatasourceConfig;
import com.github.tylerspaeth.data.entity.Symbol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class SymbolDAO {

    /**
     * Gets all the Symbols that exist.
     * @return List of Symbols
     */
    public List<Symbol> getAllSymbols() {

        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Symbol> cq = cb.createQuery(Symbol.class);
        Root<Symbol> root = cq.from(Symbol.class);
        cq.select(root);
        return entityManager.createQuery(cq).getResultList();

    }

}
