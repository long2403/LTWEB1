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
            throw new RuntimeException("DATABASE_URL environment variable is not set!");
        }

        try {
            URI uri = new URI(databaseUrl);
            String username = uri.getUserInfo().split(":")[0];
            String password = uri.getUserInfo().split(":")[1];
            String host = uri.getHost();
            String port = String.valueOf(uri.getPort());
            String dbName = uri.getPath().substring(1);

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName 
                           + "?sslmode=require&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

            System.out.println("✅ Connected to Render Postgres: " + dbName);

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DATABASE_URL", e);
        }
    }
}