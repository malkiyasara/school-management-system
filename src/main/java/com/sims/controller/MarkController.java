package com.sims.controller;

import com.sims.model.Mark;
import com.sims.model.Student;
import com.sims.model.Exam;
import com.sims.repository.MarkRepository;
import com.sims.repository.StudentRepository;
import com.sims.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MarkController {

    @Autowired
    private MarkRepository markRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamRepository examRepository;

    @GetMapping("/marks")
    public String listMarks(Model model) {
        List<Mark> marks = markRepository.findAll();
        model.addAttribute("marks", marks);
        return "marks/list";
    }

    @GetMapping("/marks/add")
    public String addMarkForm(Model model) {
        List<Student> students = studentRepository.findAll();
        List<Exam> exams = examRepository.findAll();
        
        model.addAttribute("mark", new Mark());
        model.addAttribute("students", students);
        model.addAttribute("exams", exams);
        return "marks/add";
    }

    @PostMapping("/marks/add")
    public String addMark(@ModelAttribute Mark mark, 
                         @RequestParam int studentId, 
                         @RequestParam int examId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
        
        mark.setStudent(student);
        mark.setExam(exam);
        markRepository.save(mark);
        return "redirect:/marks";
    }

    @GetMapping("/marks/edit/{id}")
    public String editMarkForm(@PathVariable int id, Model model) {
        Mark mark = markRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mark not found with id: " + id));
        List<Student> students = studentRepository.findAll();
        List<Exam> exams = examRepository.findAll();
        
        model.addAttribute("mark", mark);
        model.addAttribute("students", students);
        model.addAttribute("exams", exams);
        return "marks/edit";
    }

    @PostMapping("/marks/edit/{id}")
    public String editMark(@PathVariable int id, 
                          @RequestParam int studentId, 
                          @RequestParam int examId,
                          @RequestParam int obtainedMarks,
                          @RequestParam(required = false) String remarks) {
        Mark existingMark = markRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mark not found with id: " + id));
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
        
        existingMark.setStudent(student);
        existingMark.setExam(exam);
        existingMark.setObtainedMarks(obtainedMarks);
        existingMark.setRemarks(remarks);
        
        markRepository.save(existingMark);
        return "redirect:/marks";
    }

    @GetMapping("/marks/delete/{id}")
    public String deleteMark(@PathVariable int id) {
        if (!markRepository.existsById(id)) {
            throw new RuntimeException("Mark not found with id: " + id);
        }
        markRepository.deleteById(id);
        return "redirect:/marks";
    }

    @GetMapping("/marks/by-exam/{examId}")
    public String marksByExam(@PathVariable int examId, Model model) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
        List<Mark> marks = markRepository.findByExamId(examId);
        
        model.addAttribute("exam", exam);
        model.addAttribute("marks", marks);
        return "marks/by-exam";
    }

    @GetMapping("/marks/by-student/{studentId}")
    public String marksByStudent(@PathVariable int studentId, Model model) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        List<Mark> marks = markRepository.findByStudentId(studentId);
        
        model.addAttribute("student", student);
        model.addAttribute("marks", marks);
        return "marks/by-student";
    }
}
