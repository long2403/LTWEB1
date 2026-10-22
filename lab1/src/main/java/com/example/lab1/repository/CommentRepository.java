package com.example.lab1.repository;

import com.example.lab1.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBookIdOrderByCreatedAtDesc(Integer bookId);
    List<Comment> findAllByOrderByCreatedAtDesc();
    long countByUsernameAndRatingIsNotNullAndParentCommentIdIsNullAndDeletedFalse(String username);
    @Query("select coalesce(avg(c.rating), 0), count(c.id) from Comment c "
        + "where c.bookId = :bookId and c.rating is not null and c.parentCommentId is null and c.deleted = false")
    List<Object[]> ratingSummary(@Param("bookId") Integer bookId);
    void deleteByBookId(Integer bookId);
}