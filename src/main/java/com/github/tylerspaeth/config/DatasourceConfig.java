package com.github.tylerspaeth.config;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatasourceConfig {

    private static final HikariDataSource dataSource = new HikariDataSource();

    static {
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/algonexus");
        dataSource.setUsername("USERNAMEHERE");
        dataSource.setPassword("PASSWORDHERE");
    }

    private DatasourceConfig() {}

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
