package com.example.lab1.repository;

import com.example.lab1.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findTop15ByOrderByIdDesc();
    List<ChatMessage> findTop20ByIdGreaterThanOrderByIdAsc(Integer afterId);
    void deleteByIdLessThan(Integer id);
}
