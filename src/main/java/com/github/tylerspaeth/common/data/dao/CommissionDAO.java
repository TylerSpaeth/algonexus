package com.github.tylerspaeth.common.data.dao;

import com.github.tylerspaeth.common.data.entity.Commission;
import com.github.tylerspaeth.common.data.entity.Commission_;
import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import com.github.tylerspaeth.config.DatasourceConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class CommissionDAO extends AbstractDAO<Commission> {

    /**
     * Finds the default commission for an asset type if one exists.
     * @param assetType AssetTypeEnum
     * @return Commission or null.
     */
    public Commission findDefaultCommissionForAssetType(AssetTypeEnum assetType) {
        try (EntityManager entityManager = DatasourceConfig.entityManagerFactory.createEntityManager()) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Commission> cq = cb.createQuery(Commission.class);
            Root<Commission> root = cq.from(Commission.class);

            Predicate predicate = cb.equal(root.get(Commission_.assetType), assetType);
            predicate = cb.and(predicate, cb.isNull(root.get(Commission_.symbol)));

            cq.select(root).where(predicate);
            return entityManager.createQuery(cq).getSingleResultOrNull();
        }
    }

}
