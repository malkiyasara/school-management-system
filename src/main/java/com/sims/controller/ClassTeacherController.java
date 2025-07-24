package com.sims.controller;

import com.sims.model.AppUser;
import com.sims.model.ClassTeacher;
import com.sims.repository.ClassTeacherRepository;
import com.sims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ClassTeacherController {

    @Autowired
    private ClassTeacherRepository classTeacherRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/class-teachers")
    public String list(Model model) {
        List<ClassTeacher> assignments = classTeacherRepository.findAll();
        model.addAttribute("assignments", assignments);
        return "class-teachers/list";
    }

    @GetMapping("/class-teachers/add")
    public String addForm(Model model) {
        List<AppUser> teachers = userRepository.findAll().stream()
                .filter(u -> "TEACHER".equalsIgnoreCase(u.getRole()))
                .toList();
        model.addAttribute("teachers", teachers);
        model.addAttribute("assignment", new ClassTeacher());
        return "class-teachers/add";
    }

    @PostMapping("/class-teachers/add")
    public String add(@RequestParam String className, @RequestParam int teacherId) {
        AppUser teacher = userRepository.findById(teacherId).orElseThrow();
        ClassTeacher ct = classTeacherRepository.findByClassName(className).orElse(new ClassTeacher());
        ct.setClassName(className);
        ct.setTeacher(teacher);
        classTeacherRepository.save(ct);
        return "redirect:/class-teachers";
    }

    @GetMapping("/class-teachers/delete/{id}")
    public String delete(@PathVariable int id) {
        if (classTeacherRepository.existsById(id)) {
            classTeacherRepository.deleteById(id);
        }
        return "redirect:/class-teachers";
    }
}

