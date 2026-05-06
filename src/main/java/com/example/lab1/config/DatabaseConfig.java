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
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl == null || databaseUrl.isBlank()) {
            System.out.println("⚠️  DATABASE_URL not found → Using LOCAL PostgreSQL");
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/lab1")
                    .username("postgres")
                    .password("postgres")
                    .build();
        }

        System.out.println("✅ Using Render PostgreSQL Database");
        try {
            URI uri = new URI(databaseUrl);
            String username = uri.getUserInfo().split(":")[0];
            String password = uri.getUserInfo().split(":")[1];
            String host = uri.getHost();
            String port = String.valueOf(uri.getPort());
            String dbName = uri.getPath().substring(1);

            String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%s/%s?sslmode=require&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory",
                host, port, dbName
            );

            System.out.println("🔗 Connecting to: " + host + " / " + dbName);

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Failed to parse DATABASE_URL: " + e.getMessage());
            throw new RuntimeException("Database configuration failed", e);
        }
    }
}