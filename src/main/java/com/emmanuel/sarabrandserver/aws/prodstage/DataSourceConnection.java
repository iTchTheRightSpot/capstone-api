package com.emmanuel.sarabrandserver.aws.prodstage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/** Production only */
@Component
@Setter @Getter
@Slf4j @Profile(value = {"prod"})
public class DataSourceConnection {
    private String username;
    private String password;
    private String engine;
    private String host;
    private int port;
    private String dbname;
    private String dbInstanceIdentifier;
    private String dataSourceUrl;

    private final String awsSecret;

    public DataSourceConnection(@Qualifier(value = "awsSecretString") String awsSecret) {
        this.awsSecret = awsSecret;

        if (this.awsSecret != null) {
            try {
                var node = new ObjectMapper().readTree(this.awsSecret);

                this.username = node.get("username").textValue();
                this.password = node.get("password").textValue();
                this.engine = node.get("engine").textValue();
                this.host = node.get("host").textValue();
                this.port = (int) node.get("port").numberValue();
                this.dbname = node.get("dbname").textValue();
                this.dbInstanceIdentifier = node.get("dbInstanceIdentifier").textValue();
                this.dataSourceUrl = "jdbc:%s://%s:%s/%s".formatted(engine, host, port, dbname);
            } catch (JsonProcessingException e) {
                log.error("Data connection error in constructor of class DataSourceConnection {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url(this.dataSourceUrl);
        dataSourceBuilder.username(this.username);
        dataSourceBuilder.password(this.password);
        return dataSourceBuilder.build();
    }

}
