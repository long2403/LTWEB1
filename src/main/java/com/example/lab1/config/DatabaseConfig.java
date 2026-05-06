package com.example.lab1.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DatabaseConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource(DataSourceProperties properties, Environment env) {
        String url = properties.getUrl();
        if (url == null || url.isBlank()) {
            url = env.getProperty("DATABASE_URL");
        }
        if (url != null && url.startsWith("postgres://")) {
            url = url.replaceFirst("^postgres://", "jdbc:postgresql://");
        }
        properties.setUrl(url);
        return properties.initializeDataSourceBuilder().build();
    }
}
