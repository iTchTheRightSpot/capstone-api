package dev.webserver;

import org.testcontainers.containers.MySQLContainer;

abstract class SuperAbstract {
    protected static final MySQLContainer<?> sql;

    static {
        sql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("landscape_db")
                .withUsername("landscape")
                .withPassword("landscape");

        final String profile = System.getProperty("CI_PROFILE");
        final Boolean bool = Boolean.valueOf(profile);

        System.out.println("CLI PROFILE ARG " + profile);

        if (bool) {
            if (!sql.isCreated() || !sql.isRunning()) {
                sql.start();
                System.setProperty("SPRING_DATASOURCE_URL", sql.getJdbcUrl());
                System.setProperty("SPRING_DATASOURCE_USERNAME", sql.getUsername());
                System.setProperty("SPRING_DATASOURCE_PASSWORD", sql.getPassword());
            }
        }
    }
}
