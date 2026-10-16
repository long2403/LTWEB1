package com.example.lab1.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("!test")
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(@Value("${DATABASE_URL:}") String databaseUrl) {

        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException("DATABASE_URL environment variable is missing");
        }

        try {
            if (databaseUrl.startsWith("jdbc:")) {
                return DataSourceBuilder.create().url(databaseUrl).build();
            }
            URI uri = new URI(databaseUrl);
            String userInfo = uri.getUserInfo();
            int credentialsSeparator = userInfo == null ? -1 : userInfo.indexOf(':');
            if (credentialsSeparator < 1 || credentialsSeparator == userInfo.length() - 1) {
                throw new IllegalArgumentException("DATABASE_URL must contain a username and password");
            }

            String username = userInfo.substring(0, credentialsSeparator);
            String password = userInfo.substring(credentialsSeparator + 1);
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null || path == null || path.length() < 2) {
                throw new IllegalArgumentException("DATABASE_URL must contain a host and database name");
            }
            String dbName = path.substring(1);

            int port = uri.getPort();
            if (port <= 0) port = 5432;

            String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%d/%s?sslmode=require&ssl=true",
                host, port, dbName
            );

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("Database configuration failed", e);
        }
    }
}