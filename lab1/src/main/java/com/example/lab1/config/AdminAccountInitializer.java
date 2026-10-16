package com.example.lab1.config;

import com.example.lab1.service.ReaderAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private final ReaderAccountService accounts;
    private final String username;
    private final String password;

    public AdminAccountInitializer(
        ReaderAccountService accounts,
        @Value("${APP_ADMIN_USERNAME:}") String username,
        @Value("${APP_ADMIN_PASSWORD:}") String password
    ) {
        this.accounts = accounts;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_USERNAME and APP_ADMIN_PASSWORD must both be configured");
        }
        accounts.provisionAdmin(username, password);
    }
}
