package com.example.lab1.controller;

import com.example.lab1.entity.Student;
import com.example.lab1.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private StudentService service;

    // Hiển thị trang danh sách sinh viên
    @GetMapping("/")
    public String home(Model model) {
        List<Student> students = service.getAll();
        model.addAttribute("students", students);
        model.addAttribute("student", new Student());
        return "students";
    }

    // Hiển thị form thêm sinh viên (form trống)
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("student", new Student());
        List<Student> students = service.getAll();
        model.addAttribute("students", students);
        return "students";
    }

    // Hiển thị form sửa sinh viên (điền dữ liệu cũ)
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        Student student = service.getStudentById(id);
        model.addAttribute("student", student);
        List<Student> students = service.getAll();
        model.addAttribute("students", students);
        return "students";
    }

    // Xử lý lưu (Thêm hoặc Sửa)
    @PostMapping("/save")
    public String saveStudent(Student student, Model model) {
        try {
            if (student.getName() == null || student.getName().trim().isEmpty()) {
                model.addAttribute("error", "Tên sinh viên không được để trống!");
                model.addAttribute("students", service.getAll());
                return "students";
            }
            if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
                model.addAttribute("error", "Email không được để trống!");
                model.addAttribute("students", service.getAll());
                return "students";
            }

            service.addStudent(student);
            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi lưu: " + e.getMessage());
            model.addAttribute("students", service.getAll());
            model.addAttribute("student", student);
            return "students";
        }
    }

    // Xóa sinh viên
    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable int id) {
        service.deleteStudent(id);
        return "redirect:/";
    }

    // Tìm kiếm sinh viên
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        List<Student> students = service.findByName(keyword);
        model.addAttribute("students", students);
        model.addAttribute("student", new Student());
        model.addAttribute("keyword", keyword);
        return "students";
    }
}