package com.example.lab1.service;

import com.example.lab1.entity.ReaderAccount;
import com.example.lab1.repository.ReaderAccountRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReaderAccountService implements UserDetailsService {
    private final ReaderAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public ReaderAccountService(ReaderAccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        ReaderAccount account = accounts.findByUsernameIgnoreCase(normalizeUsername(username))
            .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        return User.withUsername(account.getUsername())
            .password(account.getPasswordHash())
            .roles(account.getRole())
            .build();
    }

    @Transactional
    public ReaderAccount register(String username, String email, String password) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (!normalizedUsername.matches("[a-z0-9_.-]{3,30}")) {
            throw new IllegalArgumentException("Tên đăng nhập phải có 3–30 ký tự: chữ, số, dấu chấm, gạch ngang hoặc gạch dưới.");
        }
        if (accounts.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
        }
        if (accounts.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }
        validatePassword(password);

        return accounts.save(new ReaderAccount(
            normalizedUsername,
            normalizedEmail,
            passwordEncoder.encode(password),
            "USER"
        ));
    }

    @Transactional
    public ReaderAccount provisionAdmin(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        validatePassword(password);
        ReaderAccount account = accounts.findByUsernameIgnoreCase(normalizedUsername).orElse(null);
        if (account == null) {
            return accounts.save(new ReaderAccount(
                normalizedUsername,
                normalizedUsername + "@storymoc.local",
                passwordEncoder.encode(password),
                "ADMIN"
            ));
        }
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            account.setPasswordHash(passwordEncoder.encode(password));
        }
        account.setRole("ADMIN");
        return accounts.save(account);
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12
            || password.getBytes(StandardCharsets.UTF_8).length > 72
            || !password.matches("(?s).*[a-z].*")
            || !password.matches("(?s).*[A-Z].*")
            || !password.matches("(?s).*\\d.*")
            || !password.matches("(?s).*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException(
                "Mật khẩu cần ít nhất 12 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt (tối đa 72 byte UTF-8)."
            );
        }
    }
}
