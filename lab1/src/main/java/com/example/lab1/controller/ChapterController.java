package com.example.lab1.controller;

import com.example.lab1.entity.Chapter;
import com.example.lab1.repository.ChapterRepository;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.entity.ReaderFavorite;
import com.example.lab1.entity.ReaderNotification;
import com.example.lab1.repository.ReaderFavoriteRepository;
import com.example.lab1.repository.ReaderNotificationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {
    private final ChapterRepository chapters;
    private final BookRepository books;
    private final ReaderFavoriteRepository favorites;
    private final ReaderNotificationRepository notifications;

    public ChapterController(
        ChapterRepository chapters,
        BookRepository books,
        ReaderFavoriteRepository favorites,
        ReaderNotificationRepository notifications
    ) {
        this.chapters = chapters;
        this.books = books;
        this.favorites = favorites;
        this.notifications = notifications;
    }

    @PostMapping
    public Chapter save(@Valid @RequestBody ChapterRequest request) {
        if (!books.existsById(request.bookId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
        if (chapters.existsByBookIdAndChapterNumber(request.bookId(), request.chapterNumber())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Số chương này đã tồn tại.");
        }
        Chapter chapter = new Chapter();
        chapter.setBookId(request.bookId());
        chapter.setChapterNumber(request.chapterNumber());
        chapter.setTitle(request.title().trim());
        chapter.setContent(request.content().trim());
        Chapter saved = chapters.save(chapter);
        String storyTitle = books.findById(saved.getBookId()).orElseThrow().getTitle();
        favorites.findByBookId(saved.getBookId()).stream()
            .map(ReaderFavorite::getUsername)
            .distinct()
            .forEach(username -> notifications.save(new ReaderNotification(
                username,
                "SYSTEM",
                "NEW_CHAPTER",
                storyTitle + " vừa có chương mới: " + saved.getTitle(),
                saved.getBookId(),
                null
            )));
        return saved;
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public Chapter update(@PathVariable Integer id, @Valid @RequestBody ChapterRequest request) {
        Chapter chapter = chapters.findById(id).orElseThrow(
            () -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Chương không tồn tại.")
        );
        if (!books.existsById(request.bookId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
        if (!chapter.getBookId().equals(request.bookId()) || !chapter.getChapterNumber().equals(request.chapterNumber())) {
            if (chapters.existsByBookIdAndChapterNumber(request.bookId(), request.chapterNumber())) {
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Số chương này đã tồn tại.");
            }
        }
        chapter.setBookId(request.bookId());
        chapter.setChapterNumber(request.chapterNumber());
        chapter.setTitle(request.title().trim());
        chapter.setContent(request.content().trim());
        return chapters.save(chapter);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        if (!chapters.existsById(id)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Chương không tồn tại.");
        }
        chapters.deleteById(id);
    }

    public record ChapterRequest(
        @NotNull @Positive Integer bookId,
        @NotNull @Positive Integer chapterNumber,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 1000000) String content
    ) {
    }
}