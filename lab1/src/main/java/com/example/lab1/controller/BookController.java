package com.example.lab1.controller;

import com.example.lab1.entity.Book;
import com.example.lab1.entity.Chapter;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import com.example.lab1.repository.CommentRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookRepository books;
    private final ChapterRepository chapters;
    private final CommentRepository comments;

    public BookController(BookRepository books, ChapterRepository chapters, CommentRepository comments) {
        this.books = books;
        this.chapters = chapters;
        this.comments = comments;
    }

    @GetMapping
    public List<Book> all(@RequestParam(required = false) String search) {
        if (search == null || search.isBlank()) return books.findAll();
        return books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search, search);
    }

    @GetMapping("/{id}")
    public Book one(@PathVariable Integer id) { return books.findById(id).orElseThrow(); }

    @PostMapping
    public Book save(@Valid @RequestBody Book book) { return books.save(book); }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public Book update(@PathVariable Integer id, @Valid @RequestBody Book book) {
        if (!books.existsById(id)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
        book.setId(id);
        return books.save(book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        if (!books.existsById(id)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
        chapters.deleteByBookId(id);
        comments.deleteByBookId(id);
        books.deleteById(id);
    }

    @GetMapping("/{id}/chapters")
    public List<Chapter> bookChapters(@PathVariable Integer id) { return chapters.findByBookIdOrderByChapterNumberAsc(id); }
}