package com.example.lab1.repository;

import com.example.lab1.entity.ReaderNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReaderNotificationRepository extends JpaRepository<ReaderNotification, Integer> {
    List<ReaderNotification> findTop50ByRecipientOrderByCreatedAtDesc(String recipient);
    long countByRecipientAndReadFalse(String recipient);
    Optional<ReaderNotification> findByIdAndRecipient(Integer id, String recipient);
    void deleteByBookId(Integer bookId);

    @Modifying
    @Query("update ReaderNotification n set n.read = true where n.recipient = :recipient and n.read = false")
    int markAllRead(@Param("recipient") String recipient);
}
