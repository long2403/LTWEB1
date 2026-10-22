package com.example.lab1.repository;

import com.example.lab1.entity.ReaderFavorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderFavoriteRepository extends JpaRepository<ReaderFavorite, Integer> {
    List<ReaderFavorite> findByUsernameOrderByCreatedAtDesc(String username);
    List<ReaderFavorite> findByBookId(Integer bookId);
    long countByUsername(String username);
    Optional<ReaderFavorite> findByUsernameAndBookId(String username, Integer bookId);
    void deleteByUsernameAndBookId(String username, Integer bookId);
    void deleteByBookId(Integer bookId);
}
