package com.sims.controller;

import com.sims.model.Fee;
import com.sims.model.Mark;
import com.sims.model.Student;
import com.sims.repository.AttendanceRepository;
import com.sims.repository.ExamRepository;
import com.sims.repository.FeeRepository;
import com.sims.repository.MarkRepository;
import com.sims.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PublicPortalController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MarkRepository markRepository;

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ExamRepository examRepository;

    @GetMapping("/portal")
    public String portalHome(HttpSession session, Model model) {
        // Check if parent is logged in
        if (session.getAttribute("parent") == null) {
            return "redirect:/parent/login";
        }

        // Add parent info to model for display
        model.addAttribute("parentName", session.getAttribute("parentName"));
        model.addAttribute("studentName", session.getAttribute("studentName"));
        model.addAttribute(
            "studentClass",
            session.getAttribute("studentClass")
        );

        return "portal/index";
    }

    // Graceful redirects if accessed as a relative path from /students
    @GetMapping("/students/portal")
    public String redirectFromStudentsPortalRoot() {
        return "redirect:/portal";
    }

    @GetMapping("/students/portal/{studentId}")
    public String redirectFromStudentsPortalStudent(
        @PathVariable int studentId
    ) {
        return "redirect:/portal/student/" + studentId;
    }

    @PostMapping("/portal/lookup")
    public String portalLookup(
        @RequestParam(required = false) Integer studentId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String className,
        HttpSession session,
        Model model
    ) {
        // Check if parent is logged in
        if (session.getAttribute("parent") == null) {
            return "redirect:/parent/login";
        }
        if (studentId != null) {
            return "redirect:/portal/student/" + studentId;
        }
        List<Student> results;
        if (
            name != null &&
            !name.trim().isEmpty() &&
            className != null &&
            !className.trim().isEmpty()
        ) {
            results =
                studentRepository.searchByFullNameAndClass(
                    name.trim(),
                    className.trim()
                );
        } else if (name != null && !name.trim().isEmpty()) {
            results = studentRepository.searchByFullName(
                name.trim()
            );
        } else if (className != null && !className.trim().isEmpty()) {
            results = studentRepository.findByClassName(className.trim());
        } else {
            results = java.util.Collections.emptyList();
        }
        model.addAttribute("results", results);
        return "portal/index";
    }

    @GetMapping("/portal/student/{studentId}")
    public String portalStudent(
        @PathVariable int studentId,
        HttpSession session,
        Model model
    ) {
        // Check if parent is logged in
        if (session.getAttribute("parent") == null) {
            return "redirect:/parent/login";
        }
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            model.addAttribute("notFound", true);
            return "portal/index";
        }
        List<Mark> marks = markRepository.findByStudentId(studentId);
        List<Fee> fees = feeRepository.findByStudentId(studentId);
        model.addAttribute("student", student);
        model.addAttribute("marks", marks);
        model.addAttribute("fees", fees);
        // Compute simple stats for template (avoid complex SpEL)
        double average = 0.0;
        double highest = 0.0;
        double lowest = 0.0;
        if (marks != null && !marks.isEmpty()) {
            average = marks
                .stream()
                .mapToDouble(Mark::getPercentage)
                .average()
                .orElse(0.0);
            highest = marks
                .stream()
                .mapToDouble(Mark::getPercentage)
                .max()
                .orElse(0.0);
            lowest = marks
                .stream()
                .mapToDouble(Mark::getPercentage)
                .min()
                .orElse(0.0);
        }
        model.addAttribute("avgPercentage", average);
        model.addAttribute("maxPercentage", highest);
        model.addAttribute("minPercentage", lowest);
        return "portal/student";
    }

    // Separate portal pages (no dashboard views)
    @GetMapping("/portal/grades")
    public String portalGrades(HttpSession session, Model model) {
        // Check if parent is logged in
        if (session.getAttribute("parent") == null) {
            return "redirect:/parent/login";
        }

        String studentName = (String) session.getAttribute("studentName");
        String studentClass = (String) session.getAttribute("studentClass");

        if (studentName != null && studentClass != null) {
            List<Student> candidates =
                studentRepository.searchByFullNameAndClass(
                    studentName,
                    studentClass
                );

            if (candidates != null && !candidates.isEmpty()) {
                Student student = candidates
                    .stream()
                    .filter(
                        s ->
                            s.getName() != null &&
                            s.getName().equalsIgnoreCase(studentName)
                    )
                    .findFirst()
                    .orElse(candidates.get(0));

                int sid = student.getId();
                List<Mark> marks = markRepository.findByStudentId(
                    sid
                );

                model.addAttribute("student", student);
                model.addAttribute("marks", marks);

                double average = 0.0,
                    highest = 0.0,
                    lowest = 0.0;
                if (marks != null && !marks.isEmpty()) {
                    average = marks
                        .stream()
                        .mapToDouble(Mark::getPercentage)
                        .average()
                        .orElse(0.0);
                    highest = marks
                        .stream()
                        .mapToDouble(Mark::getPercentage)
                        .max()
                        .orElse(0.0);
                    lowest = marks
                        .stream()
                        .mapToDouble(Mark::getPercentage)
                        .min()
                        .orElse(0.0);
                }
                model.addAttribute("avgPercentage", average);
                model.addAttribute("highestMarks", highest);
                model.addAttribute("lowestMarks", lowest);
            } else {
                // Fallback: resolve by name only if class match failed
                List<Student> nameOnly =
                    studentRepository.searchByFullName(
                        studentName
                    );
                if (nameOnly != null && !nameOnly.isEmpty()) {
                    Student student = nameOnly
                        .stream()
                        .filter(
                            s ->
                                s.getName() != null &&
                                s.getName().equalsIgnoreCase(studentName)
                        )
                        .findFirst()
                        .orElse(nameOnly.get(0));

                    int sid = student.getId();
                    List<Mark> marks = markRepository.findByStudentId(
                        sid
                    );

                    model.addAttribute("student", student);
                    model.addAttribute("marks", marks);

                    double average = 0.0,
                        highest = 0.0,
                        lowest = 0.0;
                    if (marks != null && !marks.isEmpty()) {
                        average = marks
                            .stream()
                            .mapToDouble(Mark::getPercentage)
                            .average()
                            .orElse(0.0);
                        highest = marks
                            .stream()
                            .mapToDouble(Mark::getPercentage)
                            .max()
                            .orElse(0.0);
                        lowest = marks
                            .stream()
                            .mapToDouble(Mark::getPercentage)
                            .min()
                            .orElse(0.0);
                    }
                    model.addAttribute("avgPercentage", average);
                    model.addAttribute("highestMarks", highest);
                    model.addAttribute("lowestMarks", lowest);
                } else {
                    model.addAttribute("notFound", true);
                }
            }
        } else {
            model.addAttribute("notFound", true);
        }

        return "portal/grades";
    }

    @GetMapping("/portal/attendance")
    public String portalAttendance(HttpSession session, Model model) {
        // Check if parent is logged in
        if (session.getAttribute("parent") == null) {
            return "redirect:/parent/login";
        }

        String studentName = (String) session.getAttribute("studentName");
        String studentClass = (String) session.getAttribute("studentClass");

        if (studentName != null && studentClass != null) {
            List<Student> candidates =
                studentRepository.searchByFullNameAndClass(
                    studentName,
                    studentClass
                );

            if (candidates != null && !candidates.isEmpty()) {
                Student student = candidates
                    .stream()
                    .filter(
                        s ->
                            s.getName() != null &&
                            s.getName().equalsIgnoreCase(studentName)
                    )
                    .findFirst()
                    .orElse(candidates.get(0));

                model.addAttribute("student", student);
                model.addAttribute(
                    "attendanceList",
                    attendanceRepository.findByStudentId(student.getId())
                );
            } else {
                // Fallback: resolve by name only if class match failed
                List<Student> nameOnly =
                    studentRepository.searchByFullName(
                        studentName
                    );
                if (nameOnly != null && !nameOnly.isEmpty()) {
                    Student student = nameOnly
                        .stream()
                        .filter(
                            s ->
                                s.getName() != null &&
                                s.getName().equalsIgnoreCase(studentName)
                        )
                        .findFirst()
                        .orElse(nameOnly.get(0));

                    model.addAttribute("student", student);
                    model.addAttribute(
                        "attendanceList",
                        attendanceRepository.findByStudentId(student.getId())
                    );
                } else {
                    model.addAttribute("notFound", true);
                }
            }
        } else {
            model.addAttribute("notFound", true);
        }

        return "portal/attendance";
    }

    @GetMapping("/portal/fees")
    public String portalFees(HttpSession session, Model model) {
        // Check if parent is logged in
        if (session.getAttribute("parent") == null) {
            return "redirect:/parent/login";
        }

        String studentName = (String) session.getAttribute("studentName");
        String studentClass = (String) session.getAttribute("studentClass");

        if (studentName != null && studentClass != null) {
            List<Student> candidates =
                studentRepository.searchByFullNameAndClass(
                    studentName,
                    studentClass
                );

            if (candidates != null && !candidates.isEmpty()) {
                Student student = candidates
                    .stream()
                    .filter(
                        s ->
                            s.getName() != null &&
                            s.getName().equalsIgnoreCase(studentName)
                    )
                    .findFirst()
                    .orElse(candidates.get(0));

                model.addAttribute("student", student);
                model.addAttribute(
                    "fees",
                    feeRepository.findByStudentId(student.getId())
                );
            } else {
                // Fallback: resolve by name only if class match failed
                List<Student> nameOnly =
                    studentRepository.searchByFullName(
                        studentName
                    );
                if (nameOnly != null && !nameOnly.isEmpty()) {
                    Student student = nameOnly
                        .stream()
                        .filter(
                            s ->
                                s.getName() != null &&
                                s.getName().equalsIgnoreCase(studentName)
                        )
                        .findFirst()
                        .orElse(nameOnly.get(0));

                    model.addAttribute("student", student);
                    model.addAttribute(
                        "fees",
                        feeRepository.findByStudentId(student.getId())
                    );
                } else {
                    model.addAttribute("notFound", true);
                }
            }
        } else {
            model.addAttribute("notFound", true);
        }

        return "portal/fees";
    }

    @GetMapping("/portal/timetable")
    public String portalTimetable(
        @RequestParam(required = false) String className,
        HttpSession session,
        Model model
    ) {
        // Check if parent is logged in
        if (session.getAttribute("parent") == null) {
            return "redirect:/parent/login";
        }
        if (className != null && !className.trim().isEmpty()) {
            model.addAttribute("className", className);
            model.addAttribute(
                "exams",
                examRepository
                    .findAll()
                    .stream()
                    .filter(e -> className.equalsIgnoreCase(e.getClassName()))
                    .toList()
            );
        }
        return "portal/timetable";
    }

    @GetMapping("/portal/achievements")
    public String portalAchievements(
        @RequestParam(required = false) Integer studentId,
        HttpSession session,
        Model model
    ) {
        // Check if parent is logged in
        if (session.getAttribute("parent") == null) {
            return "redirect:/parent/login";
        }
        if (studentId != null) {
            Student student = studentRepository
                .findById(studentId)
                .orElse(null);
            if (student != null) {
                var marks = markRepository.findByStudentId(studentId);
                model.addAttribute("student", student);
                model.addAttribute("marks", marks);
                double best = marks
                    .stream()
                    .mapToDouble(Mark::getPercentage)
                    .max()
                    .orElse(0.0);
                model.addAttribute("bestPercentage", best);
            } else {
                model.addAttribute("notFound", true);
            }
        }
        return "portal/achievements";
    }

    // Download CSV report for a student (marks + fees summary)
    @GetMapping("/portal/report/download")
    public ResponseEntity<byte[]> downloadStudentReport(
        @RequestParam int studentId,
        HttpSession session
    ) {
        if (session.getAttribute("parent") == null) {
            return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/parent/login")
                .build();
        }

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            byte[] body = "error,Student not found".getBytes(
                java.nio.charset.StandardCharsets.UTF_8
            );
            return ResponseEntity.badRequest()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=error.csv"
                )
                .contentType(MediaType.TEXT_PLAIN)
                .body(body);
        }

        List<Mark> marks = markRepository.findByStudentId(studentId);
        List<Fee> fees = feeRepository.findByStudentId(studentId);

        StringBuilder csv = new StringBuilder();
        csv.append("Student Report\n");
        csv.append("ID,Name,Class\n");
        csv
            .append(student.getId())
            .append(',')
            .append(escapeCsv(student.getName()))
            .append(',')
            .append(escapeCsv(student.getClassName()))
            .append('\n');
        csv.append('\n');

        csv.append("Marks\n");
        csv.append("Exam,Subject,Total,Obtained,Percentage,Remarks\n");
        for (Mark m : marks) {
            String examName = m.getExam() != null
                ? m.getExam().getExamName()
                : "";
            String subject = m.getExam() != null
                ? m.getExam().getSubject()
                : "";
            String total = m.getExam() != null
                ? String.valueOf(m.getExam().getTotalMarks())
                : "";
            csv
                .append(escapeCsv(examName))
                .append(',')
                .append(escapeCsv(subject))
                .append(',')
                .append(total)
                .append(',')
                .append(m.getObtainedMarks())
                .append(',')
                .append(
                    String.format(
                        java.util.Locale.US,
                        "%.2f",
                        m.getPercentage()
                    )
                )
                .append(',')
                .append(escapeCsv(m.getRemarks()))
                .append('\n');
        }
        csv.append('\n');

        csv.append("Fees\n");
        csv.append("FeeID,Amount,Status,Due,Paid,Description\n");
        for (Fee f : fees) {
            csv
                .append(f.getId())
                .append(',')
                .append(
                    String.format(java.util.Locale.US, "%.2f", f.getAmount())
                )
                .append(',')
                .append(escapeCsv(String.valueOf(f.getStatus())))
                .append(',')
                .append(escapeCsv(String.valueOf(f.getDueDate())))
                .append(',')
                .append(escapeCsv(String.valueOf(f.getPaidDate())))
                .append(',')
                .append(escapeCsv(f.getDescription()))
                .append('\n');
        }

        byte[] bytes = csv
            .toString()
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String filename = "student-" + student.getId() + "-report.csv";
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + filename
            )
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\r", " ").replace("\n", " ");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }
}
