package com.example.lab1.controller;

import com.example.lab1.entity.ChatMessage;
import com.example.lab1.repository.ChatMessageRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chat/messages")
public class ChatController {
    private static final int MAX_IMAGE_BYTES = 256 * 1024;
    private static final int MAX_STORED_MESSAGES = 1000;
    private static final List<String> IMAGE_TYPES = List.of("image/png", "image/jpeg", "image/gif", "image/webp");
    private final ChatMessageRepository messages;

    public ChatController(ChatMessageRepository messages) {
        this.messages = messages;
    }

    @GetMapping
    public List<ChatMessage> recent(@RequestParam(defaultValue = "0") Integer afterId) {
        if (afterId < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "afterId không hợp lệ.");
        }
        if (afterId == 0) {
            List<ChatMessage> latest = messages.findTop15ByOrderByIdDesc();
            java.util.Collections.reverse(latest);
            return latest;
        }
        return messages.findTop20ByIdGreaterThanOrderByIdAsc(afterId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessage send(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        String message = request.message() == null ? "" : request.message().trim();
        String imageData = request.imageData();
        if (message.isEmpty() && (imageData == null || imageData.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hãy nhập lời nhắn hoặc chọn ảnh.");
        }
        if (imageData != null && !imageData.isBlank()) {
            validateImage(imageData);
        } else {
            imageData = null;
        }
        ChatMessage saved = messages.save(new ChatMessage(authentication.getName(), message, imageData));
        if (saved.getId() > MAX_STORED_MESSAGES) {
            messages.deleteByIdLessThan(saved.getId() - MAX_STORED_MESSAGES);
        }
        return saved;
    }

    private void validateImage(String imageData) {
        int separator = imageData.indexOf(";base64,");
        if (!imageData.startsWith("data:image/") || separator < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh gửi lên không hợp lệ.");
        }
        String mimeType = imageData.substring("data:".length(), separator).toLowerCase(Locale.ROOT);
        if (!IMAGE_TYPES.contains(mimeType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ nhận ảnh PNG, JPG, GIF hoặc WebP.");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(imageData.substring(separator + ";base64,".length()));
            if (bytes.length == 0 || bytes.length > MAX_IMAGE_BYTES) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Ảnh tối đa 256 KB.");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh gửi lên không hợp lệ.");
        }
    }

    public record ChatRequest(
        @Size(max = 2000) String message,
        @Size(max = 700000) String imageData
    ) {
    }
}
