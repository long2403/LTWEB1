package com.example.lab1.controller;

import com.example.lab1.entity.Book;
import com.example.lab1.entity.Chapter;
import com.example.lab1.entity.ReaderNotification;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import com.example.lab1.repository.CommentRepository;
import com.example.lab1.repository.ReaderAccountRepository;
import com.example.lab1.repository.ReaderNotificationRepository;
import com.example.lab1.repository.ReaderFavoriteRepository;
import com.example.lab1.repository.ReadingHistoryRepository;
import com.example.lab1.repository.ReaderCollectionRepository;
import com.example.lab1.repository.ContentReportRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Validated
public class BookController {
    private final BookRepository books;
    private final ChapterRepository chapters;
    private final CommentRepository comments;
    private final ReaderAccountRepository accounts;
    private final ReaderNotificationRepository notifications;
    private final ReaderFavoriteRepository favorites;
    private final ReadingHistoryRepository history;
    private final ReaderCollectionRepository collections;
    private final ContentReportRepository reports;

    public BookController(
        BookRepository books,
        ChapterRepository chapters,
        CommentRepository comments,
        ReaderAccountRepository accounts,
        ReaderNotificationRepository notifications,
        ReaderFavoriteRepository favorites,
        ReadingHistoryRepository history,
        ReaderCollectionRepository collections,
        ContentReportRepository reports
    ) {
        this.books = books;
        this.chapters = chapters;
        this.comments = comments;
        this.accounts = accounts;
        this.notifications = notifications;
        this.favorites = favorites;
        this.history = history;
        this.collections = collections;
        this.reports = reports;
    }

    @GetMapping
    public List<Book> all(@RequestParam(required = false) String search) {
        if (search == null || search.isBlank()) return books.findAll();
        return books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search, search);
    }

    @GetMapping("/page")
    public Page<Book> page(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String genre,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String storyType,
        @RequestParam(defaultValue = "popular") String sort
    ) {
        if (page < 0) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Số trang không hợp lệ.");
        }
        int boundedSize = Math.max(1, Math.min(size, 48));
        Sort ordering = switch (sort.toLowerCase(Locale.ROOT)) {
            case "title" -> Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id"));
            case "new" -> Sort.by(Sort.Order.desc("id"));
            default -> Sort.by(Sort.Order.desc("views"), Sort.Order.desc("id"));
        };
        Specification<Book> filter = (root, query, builder) -> builder.conjunction();
        if (search != null && !search.isBlank()) {
            String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            filter = filter.and((root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("title")), term),
                builder.like(builder.lower(root.get("author")), term),
                builder.like(builder.lower(root.get("genre")), term),
                builder.like(builder.lower(root.get("description")), term)
            ));
        }
        if (genre != null && !genre.isBlank()) {
            filter = filter.and((root, query, builder) -> builder.equal(root.get("genre"), genre));
        }
        if (status != null && !status.isBlank()) {
            filter = filter.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        if (storyType != null && !storyType.isBlank() && !"ALL".equalsIgnoreCase(storyType)) {
            validateStoryType(storyType);
            filter = filter.and((root, query, builder) -> builder.equal(
                builder.upper(root.get("storyType")), storyType.toUpperCase(Locale.ROOT)
            ));
        }
        Pageable pageable = PageRequest.of(page, boundedSize, ordering);
        return books.findAll(filter, pageable);
    }

    @GetMapping("/genres")
    public List<String> genres() {
        return books.findDistinctGenres();
    }

    @GetMapping("/recommended")
    public List<Book> recommended() {
        return books.findTop12ByRecommendedTrueOrderByViewsDesc();
    }

    @GetMapping("/new")
    public List<Book> newest() {
        return books.findTop12ByOrderByIdDesc();
    }

    @GetMapping("/type/{storyType}")
    public List<Book> byType(@PathVariable String storyType) {
        validateStoryType(storyType);
        return books.findTop12ByStoryTypeIgnoreCaseOrderByViewsDesc(storyType);
    }

    @GetMapping("/{id}")
    public Book one(@PathVariable Integer id) {
        return books.findById(id).orElseThrow(
            () -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.")
        );
    }

    @GetMapping("/{id}/rating")
    public RatingSummary rating(@PathVariable Integer id) {
        if (!books.existsById(id)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
        Object[] values = comments.ratingSummary(id).get(0);
        return new RatingSummary(((Number) values[0]).doubleValue(), ((Number) values[1]).longValue());
    }

    @PostMapping
    @Transactional
    public Book save(@Valid @RequestBody Book book) {
        validateStoryType(book.getStoryType());
        Book saved = books.save(book);
        accounts.findAll().forEach(account -> notifications.save(new ReaderNotification(
            account.getUsername(),
            "SYSTEM",
            "NEW_BOOK",
            "Truyện mới vừa được thêm: " + saved.getTitle(),
            saved.getId(),
            null
        )));
        return saved;
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public Book update(@PathVariable Integer id, @Valid @RequestBody Book book) {
        Book existing = books.findById(id).orElseThrow(
            () -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.")
        );
        validateStoryType(book.getStoryType());
        book.setId(id);
        book.setViews(existing.getViews());
        return books.save(book);
    }

    @PostMapping("/{id}/view")
    @Transactional
    public java.util.Map<String, Integer> incrementViews(@PathVariable Integer id) {
        int updated = books.incrementViews(id);
        if (updated == 0) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
        return java.util.Map.of("views", books.findById(id).orElseThrow().getViews());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Integer id) {
        if (!books.existsById(id)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
        chapters.deleteByBookId(id);
        comments.deleteByBookId(id);
        favorites.deleteByBookId(id);
        history.deleteByBookId(id);
        notifications.deleteByBookId(id);
        collections.deleteByBookId(id);
        reports.deleteByBookId(id);
        books.deleteById(id);
    }

    @GetMapping("/{id}/chapters")
    public List<Chapter> bookChapters(@PathVariable Integer id) { return chapters.findByBookIdOrderByChapterNumberAsc(id); }

    public record RatingSummary(double average, long count) {
    }

    private void validateStoryType(String storyType) {
        if (storyType == null
            || (!"MANGA".equals(storyType.toUpperCase(Locale.ROOT))
                && !"NOVEL".equals(storyType.toUpperCase(Locale.ROOT)))) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Loại truyện chỉ được là MANGA hoặc NOVEL."
            );
        }
    }
}