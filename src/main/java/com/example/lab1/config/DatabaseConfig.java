package com.example.lab1.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = System.getenv("DATABASE_URL");

        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            // Local development
            System.out.println("=== Using Local PostgreSQL ===");
            return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/lab1")
                .username("postgres")
                .password("postgres")
                .build();
        }

        // Render PostgreSQL
        try {
            URI uri = new URI(dbUrl);
            String userInfo = uri.getUserInfo();
            String username = userInfo.split(":")[0];
            String password = userInfo.split(":")[1];
            String host = uri.getHost();
            int port = uri.getPort();
            String database = uri.getPath().substring(1);

            String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%d/%s?sslmode=require&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory",
                host, port, database
            );

            System.out.println("=== Connecting to Render Postgres ===");
            System.out.println("Host: " + host);
            System.out.println("Database: " + database);

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to configure Render DATABASE_URL", e);
        }
    }
}