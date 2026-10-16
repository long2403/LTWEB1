package com.example.lab1.controller;

import com.example.lab1.entity.ReaderFavorite;
import com.example.lab1.entity.ReadingHistory;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import com.example.lab1.repository.ReaderFavoriteRepository;
import com.example.lab1.repository.ReadingHistoryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/me")
public class ReaderLibraryController {
    private final ReaderFavoriteRepository favorites;
    private final ReadingHistoryRepository history;
    private final BookRepository books;
    private final ChapterRepository chapters;

    public ReaderLibraryController(
        ReaderFavoriteRepository favorites,
        ReadingHistoryRepository history,
        BookRepository books,
        ChapterRepository chapters
    ) {
        this.favorites = favorites;
        this.history = history;
        this.books = books;
        this.chapters = chapters;
    }

    @GetMapping("/favorites")
    public List<Integer> favorites(Authentication authentication) {
        return favorites.findByUsernameOrderByCreatedAtDesc(authentication.getName()).stream()
            .map(ReaderFavorite::getBookId)
            .toList();
    }

    @PostMapping("/favorites")
    public List<Integer> addFavorite(@Valid @RequestBody BookRequest request, Authentication authentication) {
        requireBook(request.bookId());
        favorites.findByUsernameAndBookId(authentication.getName(), request.bookId())
            .orElseGet(() -> favorites.save(new ReaderFavorite(authentication.getName(), request.bookId())));
        return favorites(authentication);
    }

    @DeleteMapping("/favorites/{bookId}")
    public List<Integer> removeFavorite(@PathVariable @Positive Integer bookId, Authentication authentication) {
        favorites.deleteByUsernameAndBookId(authentication.getName(), bookId);
        return favorites(authentication);
    }

    @GetMapping("/history")
    public List<ReadingHistory> history(Authentication authentication) {
        return history.findTop20ByUsernameOrderByUpdatedAtDesc(authentication.getName());
    }

    @PostMapping("/history")
    public ReadingHistory saveProgress(@Valid @RequestBody ReadingProgressRequest request, Authentication authentication) {
        requireBook(request.bookId());
        if (!chapters.existsByBookIdAndChapterNumber(request.bookId(), request.chapterNumber())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chương truyện không tồn tại.");
        }
        ReadingHistory progress = history.findByUsernameAndBookId(authentication.getName(), request.bookId())
            .map(existing -> {
                existing.updateChapter(request.chapterNumber());
                return existing;
            })
            .orElseGet(() -> new ReadingHistory(authentication.getName(), request.bookId(), request.chapterNumber()));
        return history.save(progress);
    }

    private void requireBook(Integer bookId) {
        if (!books.existsById(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Truyện không tồn tại.");
        }
    }

    public record BookRequest(@NotNull @Positive Integer bookId) {
    }

    public record ReadingProgressRequest(
        @NotNull @Positive Integer bookId,
        @NotNull @Positive Integer chapterNumber
    ) {
    }
}
