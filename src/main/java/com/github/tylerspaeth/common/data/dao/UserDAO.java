package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.common.data.entity.User_;
import com.github.tylerspaeth.common.enums.AccountTypeEnum;
import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class UserDAO extends AbstractDAO<User> {

    /**
     * Finds all the users that have the given account type.
     * @param accountType AccountTypeEnum
     * @return List of Users
     */
    public List<User> findUsersByAccountType(AccountTypeEnum accountType)  {
        try (EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager()) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);

            cq.select(root).where(cb.equal(root.get(User_.accountType), accountType));
            return entityManager.createQuery(cq).getResultList();
        }
    }

    /**
     * Finds the user with the matching externalAccountID or returns null.
     * @param externalAccountID ID corresponding to an external system.
     * @return User or null.
     */
    public User findUserByExternalAccountID(String externalAccountID) {
        try (EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager()) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);

            cq.select(root).where(cb.equal(root.get(User_.externalAccountID), externalAccountID));
            return entityManager.createQuery(cq).getSingleResultOrNull();
        }
    }

}
