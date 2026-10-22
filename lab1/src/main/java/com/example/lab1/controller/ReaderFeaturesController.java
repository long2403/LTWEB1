package com.example.lab1.controller;

import com.example.lab1.entity.ContentReport;
import com.example.lab1.entity.ReaderCollectionEntry;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import com.example.lab1.repository.CommentRepository;
import com.example.lab1.repository.ContentReportRepository;
import com.example.lab1.repository.ReaderCollectionRepository;
import com.example.lab1.repository.ReaderFavoriteRepository;
import com.example.lab1.repository.ReadingHistoryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ReaderFeaturesController {
    private final ReaderCollectionRepository collections;
    private final ContentReportRepository reports;
    private final BookRepository books;
    private final ChapterRepository chapters;
    private final ReadingHistoryRepository history;
    private final ReaderFavoriteRepository favorites;
    private final CommentRepository comments;

    public ReaderFeaturesController(
        ReaderCollectionRepository collections,
        ContentReportRepository reports,
        BookRepository books,
        ChapterRepository chapters,
        ReadingHistoryRepository history,
        ReaderFavoriteRepository favorites,
        CommentRepository comments
    ) {
        this.collections = collections;
        this.reports = reports;
        this.books = books;
        this.chapters = chapters;
        this.history = history;
        this.favorites = favorites;
        this.comments = comments;
    }

    @GetMapping("/api/me/collections")
    public List<ReaderCollectionEntry> collections(Authentication authentication) {
        return collections.findByUsernameOrderByCollectionNameAscCreatedAtDesc(authentication.getName());
    }

    @PostMapping("/api/me/collections")
    @Transactional
    public List<ReaderCollectionEntry> addToCollection(
        @Valid @RequestBody CollectionRequest request,
        Authentication authentication
    ) {
        requireBook(request.bookId());
        String name = request.name().trim();
        boolean exists = collections.findByUsernameOrderByCollectionNameAscCreatedAtDesc(authentication.getName()).stream()
            .anyMatch(item -> item.getBookId().equals(request.bookId())
                && item.getCollectionName().equalsIgnoreCase(name));
        if (!exists) {
            collections.save(new ReaderCollectionEntry(authentication.getName(), name, request.bookId()));
        }
        return collections(authentication);
    }

    @DeleteMapping("/api/me/collections/{id}")
    @Transactional
    public void removeFromCollection(@PathVariable @Positive Integer id, Authentication authentication) {
        ReaderCollectionEntry entry = collections.findByIdAndUsername(id, authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mục đọc không tồn tại."));
        collections.delete(entry);
    }

    @GetMapping("/api/me/achievements")
    public List<Achievement> achievements(Authentication authentication) {
        String username = authentication.getName();
        long readCount = history.countByUsername(username);
        long favoriteCount = favorites.countByUsername(username);
        long reviewCount = comments.countByUsernameAndRatingIsNotNullAndParentCommentIdIsNullAndDeletedFalse(username);
        return List.of(
            new Achievement("reader-first", "Bắt đầu hành trình", "Đọc ít nhất một bộ truyện", readCount > 0, readCount, 1),
            new Achievement("reader-five", "Độc giả chăm chỉ", "Theo dõi tiến độ 5 bộ truyện", readCount >= 5, readCount, 5),
            new Achievement("collector-five", "Người sưu tầm", "Theo dõi 5 bộ truyện", favoriteCount >= 5, favoriteCount, 5),
            new Achievement("reviewer-first", "Tiếng nói độc giả", "Đánh giá ít nhất một bộ truyện", reviewCount > 0, reviewCount, 1)
        );
    }

    @PostMapping("/api/reports")
    @Transactional
    public ContentReport report(@Valid @RequestBody ReportRequest request, Authentication authentication) {
        requireBook(request.bookId());
        if (request.chapterNumber() != null
            && !chapters.existsByBookIdAndChapterNumber(request.bookId(), request.chapterNumber())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chương truyện không tồn tại.");
        }
        return reports.save(new ContentReport(
            authentication.getName(),
            request.bookId(),
            request.chapterNumber(),
            request.reason(),
            request.details() == null ? null : request.details().trim()
        ));
    }

    private void requireBook(Integer bookId) {
        if (!books.existsById(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
    }

    public record CollectionRequest(
        @NotBlank @Size(max = 40) String name,
        @NotNull @Positive Integer bookId
    ) {
    }

    public record ReportRequest(
        @NotNull @Positive Integer bookId,
        @Positive Integer chapterNumber,
        @NotBlank @Pattern(regexp = "CONTENT_ERROR|COPYRIGHT|INAPPROPRIATE|OTHER") String reason,
        @Size(max = 1000) String details
    ) {
    }

    public record Achievement(String id, String title, String description, boolean unlocked, long progress, long target) {
    }
}
