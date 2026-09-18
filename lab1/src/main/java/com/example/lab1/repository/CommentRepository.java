package com.example.lab1.repository;

import com.example.lab1.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBookIdOrderByCreatedAtDesc(Integer bookId);
    void deleteByBookId(Integer bookId);
}