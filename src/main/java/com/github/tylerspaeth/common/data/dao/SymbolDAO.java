package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.common.data.entity.Exchange;
import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import com.github.tylerspaeth.config.DatasourceConfig;
import com.github.tylerspaeth.common.data.entity.Symbol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

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

    /**
     * Gets the symbol that matches the provided criteria
     * @param ticker The ticker of the Symbol
     * @param exchangeName The name of the exchange that this symbol is a part of
     * @param assetType The type of asset this symbol represents
     * @return The matching symbol if it exists, null otherwise
     */
    public Symbol getSymbolByCriteria(String ticker, String exchangeName, AssetTypeEnum assetType) {

        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Symbol> cq = cb.createQuery(Symbol.class);
        Root<Symbol> root = cq.from(Symbol.class);
        Join<Symbol, Exchange> join = root.join(Exchange.class);

        Predicate predicate = cb.equal(root.get("ticker"), ticker);
        predicate = cb.and(predicate, cb.equal(join.get("exchangeName"), exchangeName));
        predicate = cb.and(predicate, cb.equal(root.get("assetType"), assetType));

        cq.select(root).where(predicate);

        return entityManager.createQuery(cq).getSingleResultOrNull();
    }

}
