package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.common.data.entity.Exchange;
import com.github.tylerspaeth.common.data.entity.Exchange_;
import com.github.tylerspaeth.common.data.entity.Symbol_;
import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import com.github.tylerspaeth.config.DatasourceConfig;
import com.github.tylerspaeth.common.data.entity.Symbol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import java.util.List;

public class SymbolDAO extends AbstractDAO<Symbol> {

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

        Predicate predicate = cb.equal(root.get(Symbol_.ticker), ticker);
        predicate = cb.and(predicate, cb.equal(join.get(Exchange_.name), exchangeName));
        predicate = cb.and(predicate, cb.equal(root.get(Symbol_.assetType), assetType));

        cq.select(root).where(predicate);

        return entityManager.createQuery(cq).getSingleResultOrNull();
    }

    /**
     * Gets the matching persisted version of a non-persisted Symbol.
     * @param symbol Symbol that is not persisted.
     * @return Symbol that is persisted.
     */
    public Symbol getPersistedVersionOfSymbol(Symbol symbol) {
        // If the symbolID is set then it must be persisted already
        if(symbol.getSymbolID() != null) {
            return symbol;
        }

        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Symbol> cq = cb.createQuery(Symbol.class);
        Root<Symbol> root = cq.from(Symbol.class);

        Predicate predicate = cb.equal(root.get(Symbol_.ticker), symbol.getTicker());
        predicate = cb.and(predicate, cb.equal(root.get(Symbol_.name), symbol.getName()));
        predicate = cb.and(predicate, cb.equal(root.get(Symbol_.exchange), symbol.getExchange()));
        predicate = cb.and(predicate, cb.equal(root.get(Symbol_.assetType), symbol.getAssetType()));

        cq.select(root).where(predicate);

        return entityManager.createQuery(cq).getSingleResultOrNull();
    }

    /**
     * Find a symbol based on the provided ticker.
     * @param tickerSymbol Ticker
     * @return Symbol
     */
    public Symbol getSymbolByTicker(String tickerSymbol) {
        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Symbol> cq = cb.createQuery(Symbol.class);
        Root<Symbol> root = cq.from(Symbol.class);

        cq.select(root).where(cb.equal(root.get(Symbol_.ticker), tickerSymbol));

        return entityManager.createQuery(cq).getSingleResultOrNull();
    }

}
