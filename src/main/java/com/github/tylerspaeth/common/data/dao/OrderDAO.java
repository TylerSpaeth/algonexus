package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.Order_;
import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class OrderDAO extends AbstractDAO<Order> {

    /**
     * Gets all orders that are not in the finalized status.
     * @param user User to find orders for.
     * @return List of all non-finalized orders.
     */
    public List<Order> getOpenOrdersForUser(User user) {
        EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> root = cq.from(Order.class);

        Predicate predicate = cb.equal(root.get(Order_.user), user);
        predicate = cb.and(predicate, cb.isTrue(root.get(Order_.finalized)));

        cq.select(root).where(predicate);
        return entityManager.createQuery(cq).getResultList();
    }

}
