package com.example.lab1.repository;

import com.example.lab1.entity.ReadingHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Integer> {
    List<ReadingHistory> findTop20ByUsernameOrderByUpdatedAtDesc(String username);
    void deleteByBookId(Integer bookId);
    Optional<ReadingHistory> findByUsernameAndBookId(String username, Integer bookId);
}
