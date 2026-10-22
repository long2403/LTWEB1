package com.example.lab1.repository;

import com.example.lab1.entity.ReaderCollectionEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderCollectionRepository extends JpaRepository<ReaderCollectionEntry, Integer> {
    List<ReaderCollectionEntry> findByUsernameOrderByCollectionNameAscCreatedAtDesc(String username);
    Optional<ReaderCollectionEntry> findByIdAndUsername(Integer id, String username);
    void deleteByBookId(Integer bookId);
}
