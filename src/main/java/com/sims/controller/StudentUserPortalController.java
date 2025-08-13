package com.sims.controller;

import com.sims.model.Attendance;
import com.sims.model.Mark;
import com.sims.model.Student;
import com.sims.repository.AttendanceRepository;
import com.sims.repository.MarkRepository;
import com.sims.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student_user")
public class StudentUserPortalController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private MarkRepository markRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Student sessionUser = (Student) session.getAttribute("studentUser");
        if (sessionUser == null) return "redirect:/student/login";

        Student user = studentRepository.findById(sessionUser.getId()).orElse(sessionUser);
        session.setAttribute("studentUser", user);
        model.addAttribute("studentUser", user);

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();

        List<Attendance> monthlyAttendance =
                attendanceRepository.findByStudentIdAndDateRange(user.getId(), startOfMonth, endOfMonth);

        long totalDays = monthlyAttendance.size();
        long presentDays = monthlyAttendance.stream()
                .filter(a -> "Present".equalsIgnoreCase(a.getStatus()))
                .count();

        double attendancePercentage = totalDays > 0
                ? (double) presentDays / totalDays * 100
                : 0;

        // Marks
        List<Mark> marks = markRepository.findByStudentId(user.getId());

        List<String> examLabels = marks.stream()
                .map(m -> m.getExam() != null ? m.getExam().getExamName() : "Exam")
                .collect(Collectors.toList());

        List<Double> percentages = marks.stream()
                .map(m -> {
                    if (m.getExam() != null && m.getExam().getTotalMarks() > 0) {
                        return (double) m.getObtainedMarks() / m.getExam().getTotalMarks() * 100;
                    } else {
                        return 0.0;
                    }
                })
                .collect(Collectors.toList());

        double avgPercentage = percentages.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Class average
        List<Student> classmates = studentRepository.findByClassName(user.getClassName());
        List<Mark> classMarks = classmates.stream()
                .flatMap(s -> markRepository.findByStudentId(s.getId()).stream())
                .toList();

        double classAvgPercentage = classMarks.stream()
                .mapToDouble(m -> {
                    if (m.getExam() != null && m.getExam().getTotalMarks() > 0) {
                        return (double) m.getObtainedMarks() / m.getExam().getTotalMarks() * 100;
                    } else {
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);

        // Add to model
        model.addAttribute("presentDays", presentDays);
        model.addAttribute("totalDays", totalDays);
        model.addAttribute("attendancePercentage", attendancePercentage);
        model.addAttribute("marks", marks);
        model.addAttribute("avgPercentage", String.format("%.2f", avgPercentage));
        model.addAttribute("classAvgPercentage", String.format("%.2f", classAvgPercentage));
        model.addAttribute("examLabels", examLabels);
        model.addAttribute("percentages", percentages);

        return "student_user/dashboard";
    }

    // Attendance
    @GetMapping("/attendance")
    public String attendancePage(HttpSession session, Model model) {
        Student sessionUser = (Student) session.getAttribute("studentUser");
        if (sessionUser == null) return "redirect:/student/login";

        Student user = studentRepository.findById(sessionUser.getId()).orElse(sessionUser);
        session.setAttribute("studentUser", user);
        model.addAttribute("studentUser", user);

        // Attendance list sorted by date
        List<Attendance> attendanceList = attendanceRepository.findByStudentId(user.getId())
                .stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .toList();

        long presentCount = attendanceList.stream().filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();
        long absentCount = attendanceList.stream().filter(a -> "Absent".equalsIgnoreCase(a.getStatus())).count();
        long lateCount = attendanceList.stream().filter(a -> "Late".equalsIgnoreCase(a.getStatus())).count();

        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("attendanceCounts",
                Map.of("Present", presentCount, "Absent", absentCount, "Late", lateCount));

        return "student_user/attendance";
    }

    // Profile
    @GetMapping("/profile")
    public String profileForm(HttpSession session, Model model) {
        Student sessionUser = (Student) session.getAttribute("studentUser");
        if (sessionUser == null) return "redirect:/student/login";

        Student user = studentRepository.findById(sessionUser.getId()).orElse(sessionUser);
        session.setAttribute("studentUser", user);
        model.addAttribute("studentUser", user);

        return "student_user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phoneNumber,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String className,
                                @RequestParam(required = false) String password,
                                @RequestParam(required = false) String confirmPassword,
                                HttpSession session,
                                Model model) {
        Student current = (Student) session.getAttribute("studentUser");
        if (current == null) return "redirect:/student/login";

        if (password != null && !password.isBlank() && !password.equals(confirmPassword)) {
            model.addAttribute("studentUser", current);
            model.addAttribute("error", "Passwords do not match!");
            return "student_user/profile";
        }

        current.setFirstName(firstName);
        current.setLastName(lastName);
        current.setEmail(email);
        current.setPhoneNumber(phoneNumber);
        current.setAddress(address);
        current.setClassName(
                (className == null || className.isBlank()) ? "Not Assigned" : className
        );

        if (password != null && !password.isBlank()) {
            current.setPasswordHash(passwordEncoder.encode(password));
        }

        Student updated = studentRepository.save(current);

        session.setAttribute("studentUser", updated);

        model.addAttribute("studentUser", updated);
        model.addAttribute("success", "Profile updated successfully!");
        return "student_user/profile";
    }

    // Delete & Logout
    @PostMapping("/delete")
    public String deleteAccount(HttpSession session) {
        Student user = (Student) session.getAttribute("studentUser");
        if (user == null) return "redirect:/student/login";

        studentRepository.deleteById(user.getId());
        session.invalidate();
        return "redirect:/student/register?success=Account deleted. You can register again.";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/student/login?success=You have been logged out successfully.";
    }
}
