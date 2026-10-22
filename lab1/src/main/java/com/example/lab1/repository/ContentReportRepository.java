package com.example.lab1.repository;

import com.example.lab1.entity.ContentReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentReportRepository extends JpaRepository<ContentReport, Integer> {
    List<ContentReport> findAllByOrderByStatusAscCreatedAtDesc();
    void deleteByBookId(Integer bookId);
}
