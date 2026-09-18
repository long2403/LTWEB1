package com.example.lab1.controller;

import com.example.lab1.entity.Book;
import com.example.lab1.entity.Chapter;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
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

    public BookController(BookRepository books, ChapterRepository chapters) {
        this.books = books;
        this.chapters = chapters;
    }

    @GetMapping
    public List<Book> all(@RequestParam(required = false) String search) {
        if (search == null || search.isBlank()) return books.findAll();
        return books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search, search);
    }

    @GetMapping("/{id}")
    public Book one(@PathVariable Integer id) { return books.findById(id).orElseThrow(); }

    @PostMapping
    public Book save(@RequestBody Book book) { return books.save(book); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) { chapters.deleteByBookId(id); books.deleteById(id); }

    @GetMapping("/{id}/chapters")
    public List<Chapter> bookChapters(@PathVariable Integer id) { return chapters.findByBookIdOrderByChapterNumberAsc(id); }
}