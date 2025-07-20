package com.sims.controller;

import com.sims.model.Attendance;
import com.sims.model.Student;
import com.sims.repository.AttendanceRepository;
import com.sims.repository.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/attendance")
    public String listAttendance(Model model) {
        List<Attendance> attendanceList = attendanceRepository.findAll();
        model.addAttribute("attendanceList", attendanceList);
        return "Attendance/list";
    }

    @GetMapping("/attendance/mark")
    public String markAttendanceForm(Model model) {
        List<Student> students = studentRepository.findAll();
        model.addAttribute("students", students);
        model.addAttribute("attendance", new Attendance());
        model.addAttribute("today", LocalDate.now());
        return "Attendance/add";
    }

    @PostMapping("/attendance/mark")
    public String markAttendance(
        @ModelAttribute Attendance attendance,
        @RequestParam int studentId,
        @RequestParam String status,
        @RequestParam(required = false) String remarks
    ) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new RuntimeException("Student not found with id: " + studentId)
            );

        attendance.setStudent(student);
        attendance.setStatus(status);
        attendance.setRemarks(remarks);

        attendanceRepository.save(attendance);
        return "redirect:/attendance";
    }

    @GetMapping("/attendance/edit/{id}")
    public String editAttendanceForm(@PathVariable int id, Model model) {
        Attendance attendance = attendanceRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Attendance not found with id: " + id)
            );

        model.addAttribute("attendance", attendance);
        return "Attendance/edit";
    }

    @PostMapping("/attendance/edit/{id}")
    public String editAttendance(
        @PathVariable int id,
        @RequestParam String status,
        @RequestParam(required = false) String remarks
    ) {
        Attendance existingAttendance = attendanceRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Attendance not found with id: " + id)
            );

        existingAttendance.setStatus(status);
        existingAttendance.setRemarks(remarks);

        attendanceRepository.save(existingAttendance);
        return "redirect:/attendance";
    }

    @GetMapping("/attendance/delete/{id}")
    public String deleteAttendance(@PathVariable int id) {
        if (!attendanceRepository.existsById(id)) {
            throw new RuntimeException("Attendance not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
        return "redirect:/attendance";
    }

    @GetMapping("/attendance/by-date")
    public String attendanceByDate(@RequestParam LocalDate date, Model model) {
        List<Attendance> attendanceList = attendanceRepository.findByDate(date);
        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("selectedDate", date);
        return "Attendance/by-date";
    }

    @GetMapping("/attendance/by-student/{studentId}")
    public String attendanceByStudent(
        @PathVariable int studentId,
        Model model
    ) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new RuntimeException("Student not found with id: " + studentId)
            );
        List<Attendance> attendanceList = attendanceRepository.findByStudentId(
            studentId
        );

        // Calculate attendance statistics
        long totalDays = attendanceRepository.countByStudentId(studentId);
        long presentDays = attendanceRepository.countByStudentIdAndStatus(
            studentId,
            "Present"
        );
        long absentDays = attendanceRepository.countByStudentIdAndStatus(
            studentId,
            "Absent"
        );
        long lateDays = attendanceRepository.countByStudentIdAndStatus(
            studentId,
            "Late"
        );

        double attendancePercentage = totalDays > 0
            ? ((double) presentDays / totalDays) * 100
            : 0;

        model.addAttribute("student", student);
        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("totalDays", totalDays);
        model.addAttribute("presentDays", presentDays);
        model.addAttribute("absentDays", absentDays);
        model.addAttribute("lateDays", lateDays);
        model.addAttribute("attendancePercentage", attendancePercentage);

        return "Attendance/by-student";
    }

    @GetMapping("/attendance/by-class")
    public String attendanceByClass(
        @RequestParam String className,
        Model model
    ) {
        List<Attendance> attendanceList = attendanceRepository.findByClassName(
            className
        );
        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("className", className);
        return "Attendance/by-class";
    }
}
