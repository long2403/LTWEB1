package com.example.lab1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lab1.entity.Student;
import com.example.lab1.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;

    public Student addStudent(Student student) {
        // Nếu ID = null hoặc 0, JPA sẽ auto-generate (INSERT)
        // Nếu ID có giá trị, JPA sẽ UPDATE
        if (student.getId() != null && student.getId() == 0) {
            student.setId(null);
        }
        return repository.save(student);
    }

    public void deleteStudent(int id) {
        repository.deleteById(id);
    }

    public List<Student> findByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public List<Student> getAll() {
        return repository.findAll();
    }

    public Student getStudentById(int id) {
        return repository.findById(id).orElse(null);
    }
}