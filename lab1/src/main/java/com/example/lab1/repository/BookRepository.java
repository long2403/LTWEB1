package com.example.lab1.repository;

import com.example.lab1.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(b.views), 0) from Book b")
    long sumViews();
}