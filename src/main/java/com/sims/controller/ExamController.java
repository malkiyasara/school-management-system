package com.sims.controller;

import com.sims.model.Exam;
import com.sims.model.Mark;
import com.sims.repository.ExamRepository;
import com.sims.repository.MarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ExamController {

    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private MarkRepository markRepository;

    @GetMapping("/exams")
    public String listExams(Model model) {
        List<Exam> exams = examRepository.findAll();
        model.addAttribute("exams", exams);
        return "exams/list";
    }

    @GetMapping("/exams/add")
    public String addExamForm(Model model) {
        model.addAttribute("exam", new Exam());
        return "exams/add";
    }

    @PostMapping("/exams/add")
    public String addExam(@ModelAttribute Exam exam) {
        try {
            examRepository.save(exam);
            return "redirect:/exams?success=Exam added successfully";
        } catch (Exception e) {
            return "redirect:/exams/add?error=Failed to add exam. Please check your input.";
        }
    }

    @GetMapping("/exams/edit/{id}")
    public String editExamForm(@PathVariable int id, Model model) {
        Exam exam = examRepository.findById(id).orElseThrow();
        model.addAttribute("exam", exam);
        return "exams/edit";
    }

    @PostMapping("/exams/edit/{id}")
    public String editExam(@PathVariable int id, @ModelAttribute Exam exam) {
        try {
            exam.setId(id);
            examRepository.save(exam);
            return "redirect:/exams?success=Exam updated successfully";
        } catch (Exception e) {
            return "redirect:/exams/edit/" + id + "?error=Failed to update exam. Please check your input.";
        }
    }

    @GetMapping("/exams/delete/{id}")
    public String deleteExam(@PathVariable int id) {
        try {
            // First, delete all marks associated with this exam
            List<Mark> examMarks = markRepository.findByExamId(id);
            if (!examMarks.isEmpty()) {
                markRepository.deleteAll(examMarks);
            }
            
            // Then delete the exam
            examRepository.deleteById(id);
            return "redirect:/exams?success=Exam deleted successfully";
        } catch (Exception e) {
            return "redirect:/exams?error=Failed to delete exam. It may have associated marks.";
        }
    }
}
