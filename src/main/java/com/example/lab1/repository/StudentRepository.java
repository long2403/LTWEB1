package com.example.lab1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lab1.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    List<Student> findByNameContainingIgnoreCase(String name);
}
