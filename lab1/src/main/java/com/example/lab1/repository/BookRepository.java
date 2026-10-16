package com.example.lab1.repository;

import com.example.lab1.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    java.util.Optional<Book> findFirstByTitleIgnoreCase(String title);
    @Query("select distinct b.genre from Book b where b.genre is not null and trim(b.genre) <> '' order by b.genre")
    List<String> findDistinctGenres();
    List<Book> findTop12ByRecommendedTrueOrderByViewsDesc();
    List<Book> findTop12ByOrderByIdDesc();
    List<Book> findTop12ByStoryTypeIgnoreCaseOrderByViewsDesc(String storyType);

    @Modifying
    @Query("update Book b set b.views = coalesce(b.views, 0) + 1 where b.id = :id")
    int incrementViews(@Param("id") Integer id);

    @Query("select coalesce(sum(b.views), 0) from Book b")
    long sumViews();
}