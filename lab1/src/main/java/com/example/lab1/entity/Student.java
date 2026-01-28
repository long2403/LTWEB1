package com.example.lab1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ⭐ BẮT BUỘC
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    // ✅ Constructor rỗng (BẮT BUỘC cho JPA)
    public Student() {
    }

    // ✅ Constructor KHÔNG CÓ id
    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // ===== getter & setter =====
    public Integer getId() {
        return id;
    }

    // ❌ KHÔNG setId khi thêm mới (Hibernate tự lo)
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
