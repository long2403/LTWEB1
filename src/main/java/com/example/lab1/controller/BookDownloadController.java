package com.example.lab1.controller;

import com.example.lab1.entity.Book;
import com.example.lab1.entity.Chapter;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/books/{bookId}/download")
public class BookDownloadController {
    private final BookRepository books;
    private final ChapterRepository chapters;

    public BookDownloadController(BookRepository books, ChapterRepository chapters) {
        this.books = books;
        this.chapters = chapters;
    }

    @GetMapping
    public ResponseEntity<byte[]> download(@PathVariable Integer bookId, @RequestParam String format) throws Exception {
        Book book = books.findById(bookId).orElseThrow();
        List<Chapter> items = chapters.findByBookIdOrderByChapterNumberAsc(bookId);
        String text = text(book, items);
        String normalized = format.toLowerCase();
        byte[] data;
        String extension;
        MediaType type;
        if ("epub".equals(normalized)) {
            data = epub(book, text);
            extension = "epub";
            type = MediaType.APPLICATION_OCTET_STREAM;
        } else if ("pdf".equals(normalized)) {
            data = pdf(text);
            extension = "pdf";
            type = MediaType.APPLICATION_PDF;
        } else {
            data = text.getBytes(StandardCharsets.UTF_8);
            extension = "txt";
            type = MediaType.TEXT_PLAIN;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.setContentDisposition(ContentDisposition.attachment().filename(safe(book.getTitle()) + "." + extension).build());
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private String text(Book book, List<Chapter> items) {
        StringBuilder output = new StringBuilder(book.getTitle()).append("\n\n");
        for (Chapter chapter : items) output.append("Chương ").append(chapter.getChapterNumber()).append(": ").append(chapter.getTitle()).append("\n\n").append(chapter.getContent()).append("\n\n");
        return output.toString();
    }

    private byte[] epub(Book book, String text) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry("mimetype")); zip.write("application/epub+zip".getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            zip.putNextEntry(new ZipEntry("OEBPS/content.xhtml")); zip.write(("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>" + safe(book.getTitle()) + "</title></head><body><pre>" + escape(text) + "</pre></body></html>").getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
        }
        return bytes.toByteArray();
    }

    private byte[] pdf(String text) {
        String escaped = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("\r", "").replace("\n", ") Tj 0 -16 Td (" );
        String body = "BT /F1 10 Tf 40 760 Td (" + escaped + ") Tj ET";
        String pdf = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 595 842]/Resources<</Font<</F1 4 0 R>>>>/Contents 5 0 R>>endobj\n4 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n5 0 obj<</Length " + body.length() + ">>stream\n" + body + "\nendstream endobj\ntrailer<</Root 1 0 R>>\n%%EOF";
        return pdf.getBytes(StandardCharsets.US_ASCII);
    }

    private String safe(String value) { return value == null ? "book" : value.replaceAll("[^a-zA-Z0-9-_ ]", "").trim().replace(' ', '-'); }
    private String escape(String value) { return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
}