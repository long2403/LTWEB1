package com.example.lab1.repository;

import com.example.lab1.entity.ReaderAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderAccountRepository extends JpaRepository<ReaderAccount, Integer> {
    Optional<ReaderAccount> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
}
