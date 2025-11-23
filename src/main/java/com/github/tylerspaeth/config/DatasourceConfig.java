package com.github.tylerspaeth.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatasourceConfig {

    public static final EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("persistenceUnit");
    }

}
