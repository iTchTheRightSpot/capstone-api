package dev.webserver;

import org.testcontainers.containers.MySQLContainer;

abstract class SuperAbstract {

    protected static final MySQLContainer<?> sql;

    static {
        sql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("capstone_db")
                .withUsername("capstone")
                .withPassword("capstone");

        if (Boolean.parseBoolean(System.getProperty("CI_PROFILE"))) {
            if (!sql.isCreated() || !sql.isRunning()) {
                sql.start();
                System.setProperty("SPRING_DATASOURCE_URL", sql.getJdbcUrl());
                System.setProperty("SPRING_DATASOURCE_USERNAME", sql.getUsername());
                System.setProperty("SPRING_DATASOURCE_PASSWORD", sql.getPassword());
            }
        }
    }

}
