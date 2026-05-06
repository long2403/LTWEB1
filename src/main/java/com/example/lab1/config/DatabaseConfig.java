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

        // Local development fallback
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/lab1")  // thay tên DB local nếu khác
                    .username("postgres")
                    .password("postgres")
                    .build();
        }

        // Render PostgreSQL
        try {
            URI uri = new URI(databaseUrl);
            String username = uri.getUserInfo().split(":")[0];
            String password = uri.getUserInfo().split(":")[1];
            String host = uri.getHost();
            String port = String.valueOf(uri.getPort());
            String dbName = uri.getPath().substring(1);

            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DATABASE_URL", e);
        }
    }
}