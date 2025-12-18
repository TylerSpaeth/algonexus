package com.github.tylerspaeth.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceConfig.class);

    public static final EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("persistenceUnit");
    }

    /**
     * Triggers Hibernate to validate the Entities against the database.
     */
    public static void validate() {
        LOGGER.info("Database Validation Successful");
    }

}
