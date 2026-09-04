package com.example.lab1.controller;

import com.example.lab1.entity.Chapter;
import com.example.lab1.repository.ChapterRepository;
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

    public ChapterController(ChapterRepository chapters) { this.chapters = chapters; }

    @PostMapping
    public Chapter save(@RequestBody Chapter chapter) { return chapters.save(chapter); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) { chapters.deleteById(id); }
}