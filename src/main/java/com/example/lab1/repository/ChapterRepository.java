package com.example.lab1.repository;

import com.example.lab1.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    List<Chapter> findByBookIdOrderByChapterNumberAsc(Integer bookId);
    long countByBookId(Integer bookId);
    void deleteByBookId(Integer bookId);
}