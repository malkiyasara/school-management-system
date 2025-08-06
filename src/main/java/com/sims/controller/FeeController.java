package com.sims.controller;

import com.sims.model.Fee;
import com.sims.model.Student;
import com.sims.repository.FeeRepository;
import com.sims.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class FeeController {

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/fees")
    public String listFees(Model model) {
        List<Fee> fees = feeRepository.findAll();
        model.addAttribute("fees", fees);
        return "fees/list";
    }

    @GetMapping("/fees/add")
    public String addFeeForm(Model model) {
        List<Student> students = studentRepository.findAll();
        model.addAttribute("students", students);
        model.addAttribute("fee", new Fee());
        model.addAttribute("today", LocalDate.now());
        return "fees/add";
    }

    @PostMapping("/fees/add")
    public String addFee(@ModelAttribute Fee fee, @RequestParam int studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        fee.setStudent(student);
        feeRepository.save(fee);
        return "redirect:/fees";
    }

    @GetMapping("/fees/edit/{id}")
    public String editFeeForm(@PathVariable int id, Model model) {
        Fee fee = feeRepository.findById(id).orElseThrow();
        List<Student> students = studentRepository.findAll();
        model.addAttribute("fee", fee);
        model.addAttribute("students", students);
        return "fees/edit";
    }

    @PostMapping("/fees/edit/{id}")
    public String editFee(@PathVariable int id,
                          @RequestParam int studentId,
                          @RequestParam double amount,
                          @RequestParam String status,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false) LocalDate dueDate,
                          @RequestParam(required = false) LocalDate paidDate) {
        Fee existingFee = feeRepository.findById(id).orElseThrow();
        Student student = studentRepository.findById(studentId).orElseThrow();
        existingFee.setStudent(student);
        existingFee.setAmount(amount);
        existingFee.setStatus(status);
        existingFee.setDescription(description);
        existingFee.setDueDate(dueDate);
        existingFee.setPaidDate(paidDate);
        feeRepository.save(existingFee);
        return "redirect:/fees";
    }

    @GetMapping("/fees/delete/{id}")
    public String deleteFee(@PathVariable int id) {
        if (!feeRepository.existsById(id)) {
            throw new RuntimeException("Fee not found with id: " + id);
        }
        feeRepository.deleteById(id);
        return "redirect:/fees";
    }

    @GetMapping("/fees/by-student/{studentId}")
    public String feesByStudent(@PathVariable int studentId, Model model) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<Fee> fees = feeRepository.findByStudentId(studentId);
        model.addAttribute("student", student);
        model.addAttribute("fees", fees);
        return "fees/by-student";
    }

    @GetMapping("/fees/overdue")
    public String overdueFees(Model model) {
        List<Fee> fees = feeRepository.findByDueDateBefore(LocalDate.now());
        model.addAttribute("fees", fees);
        model.addAttribute("asOf", LocalDate.now());
        return "fees/overdue";
    }
}

