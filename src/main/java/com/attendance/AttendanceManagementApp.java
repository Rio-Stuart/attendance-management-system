package com.attendance;

import com.attendance.config.DatabaseConfig;
import com.attendance.controller.*;
import com.attendance.dto.*;
import com.attendance.model.*;
import com.attendance.model.enums.*;
import com.attendance.repository.*;
import com.attendance.repository.impl.*;
import com.attendance.service.*;
import com.attendance.service.impl.*;

import java.util.*;

public class AttendanceManagementApp {

    // Repositories
    private static UserRepository userRepository;
    private static SectionRepository sectionRepository;
    private static SubjectRepository subjectRepository;
    private static SectionSubjectRepository sectionSubjectRepository;
    private static StudentEnrollmentRepository enrollmentRepository;
    private static ClassSessionRepository classSessionRepository;
    private static AttendanceRepository attendanceRepository;
    private static TimetableRepository timetableRepository;
    private static HolidayRepository holidayRepository;
    private static AttendanceAuditRepository auditRepository;

    // Services
    private static ClassSessionService classSessionService;
    private static AttendanceService attendanceService;
    private static AttendanceCalculationService calculationService;
    private static TimetableGenerationService timetableService;
    private static AttendancePredictionService predictionService;
    private static AttendanceReportService reportService;

    // Controllers
    private static ClassSessionController sessionController;
    private static AttendanceController attendanceController;
    private static ReportController reportController;
    private static TimetableController timetableController;
    private static PredictionController predictionController;

    private static User loggedInUser = null;

    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("   ATTENDANCE MANAGEMENT SYSTEM");
        System.out.println("   OOP Course Project");
        System.out.println("=============================================\n");

        // Initialize database
        System.out.println("[*] Initializing database...");
        DatabaseConfig config = DatabaseConfig.getInstance();
        config.initializeDatabase();
        config.seedDatabase();
        System.out.println("[+] Database initialized and seeded.\n");

        // Initialize all layers
        initializeRepositories();
        initializeServices();
        initializeControllers();

        // Main loop
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            if (loggedInUser == null) {
                running = showLoginMenu(scanner);
            } else if (loggedInUser.getRole() == UserRole.ADMIN) {
                showAdminMenu(scanner);
            } else {
                showStudentMenu(scanner);
            }
        }

        System.out.println("\nGoodbye!");
        scanner.close();
    }

    private static void initializeRepositories() {
        userRepository = new JdbcUserRepository();
        sectionRepository = new JdbcSectionRepository();
        subjectRepository = new JdbcSubjectRepository();
        sectionSubjectRepository = new JdbcSectionSubjectRepository();
        enrollmentRepository = new JdbcStudentEnrollmentRepository();
        classSessionRepository = new JdbcClassSessionRepository();
        attendanceRepository = new JdbcAttendanceRepository();
        timetableRepository = new JdbcTimetableRepository();
        holidayRepository = new JdbcHolidayRepository();
        auditRepository = new JdbcAttendanceAuditRepository();
    }

    private static void initializeServices() {
        classSessionService = new ClassSessionServiceImpl(classSessionRepository, sectionSubjectRepository);
        attendanceService = new AttendanceServiceImpl(attendanceRepository, classSessionRepository,
                enrollmentRepository, auditRepository);
        calculationService = new AttendanceCalculationServiceImpl(attendanceRepository,
                classSessionRepository, enrollmentRepository, sectionSubjectRepository);
        timetableService = new TimetableGenerationServiceImpl(timetableRepository,
                classSessionRepository, holidayRepository, sectionSubjectRepository);
        predictionService = new AttendancePredictionServiceImpl(calculationService,
                classSessionRepository, enrollmentRepository, holidayRepository);
        reportService = new AttendanceReportServiceImpl(calculationService, enrollmentRepository,
                sectionSubjectRepository, userRepository, sectionRepository, subjectRepository);
    }

    private static void initializeControllers() {
        sessionController = new ClassSessionController(classSessionService);
        attendanceController = new AttendanceController(attendanceService, enrollmentRepository);
        reportController = new ReportController(reportService, calculationService);
        timetableController = new TimetableController(timetableService, timetableRepository);
        predictionController = new PredictionController(predictionService);
    }

    // ===================== LOGIN =====================

    private static boolean showLoginMenu(Scanner scanner) {
        System.out.println("=== LOGIN ===");
        System.out.println("1. Admin Login");
        System.out.println("2. Student Login");
        System.out.println("0. Exit");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": case "2":
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Password: ");
                String password = scanner.nextLine().trim();
                Optional<User> user = userRepository.authenticate(username, password);
                if (user.isPresent()) {
                    loggedInUser = user.get();
                    boolean isAdmin = loggedInUser.getRole() == UserRole.ADMIN;
                    if ((choice.equals("1") && !isAdmin) || (choice.equals("2") && isAdmin)) {
                        System.out.println("[!] Role mismatch. Please use the correct login option.\n");
                        loggedInUser = null;
                    } else {
                        System.out.println("[+] Welcome, " + loggedInUser.getFullName() + "!\n");
                    }
                } else {
                    System.out.println("[!] Invalid credentials.\n");
                }
                return true;
            case "0":
                return false;
            default:
                System.out.println("[!] Invalid choice.\n");
                return true;
        }
    }

    // ===================== ADMIN MENU =====================

    private static void showAdminMenu(Scanner scanner) {
        System.out.println("\n--- ADMIN MENU (" + loggedInUser.getFullName() + ") ---");
        System.out.println("1. Create Class Session");
        System.out.println("2. Mark Attendance");
        System.out.println("3. Finalize Attendance");
        System.out.println("4. View Section Report");
        System.out.println("5. View Below 75% Report");
        System.out.println("6. Generate Timetable Sessions");
        System.out.println("7. View Timetable");
        System.out.println("8. Cancel a Session");
        System.out.println("0. Logout");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": handleCreateSession(scanner); break;
            case "2": handleMarkAttendance(scanner); break;
            case "3": handleFinalizeAttendance(scanner); break;
            case "4": handleSectionReport(scanner); break;
            case "5": handleBelowThreshold(scanner); break;
            case "6": handleGenerateSessions(scanner); break;
            case "7": handleViewTimetable(scanner); break;
            case "8": handleCancelSession(scanner); break;
            case "0": loggedInUser = null; System.out.println("Logged out.\n"); break;
            default: System.out.println("[!] Invalid choice.");
        }
    }

    private static void handleCreateSession(Scanner scanner) {
        try {
            System.out.println("\n--- Available Sections ---");
            List<Section> sections = sectionRepository.findAll();
            sections.forEach(s -> System.out.println("  [" + s.getId() + "] " + s.getSectionName()));

            System.out.print("Section ID: ");
            long sectionId = Long.parseLong(scanner.nextLine().trim());

            System.out.println("\n--- Subjects for this Section ---");
            List<SectionSubject> ssList = sectionSubjectRepository.findBySectionId(sectionId);
            for (SectionSubject ss : ssList) {
                Subject subj = subjectRepository.findById(ss.getSubjectId()).orElse(null);
                System.out.println("  [" + ss.getId() + "] " + (subj != null ? subj.getSubjectName() : "Unknown"));
            }

            System.out.print("Section-Subject ID: ");
            long ssId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Date (yyyy-MM-dd): ");
            String date = scanner.nextLine().trim();
            System.out.print("Start time (HH:mm): ");
            String start = scanner.nextLine().trim();
            System.out.print("End time (HH:mm): ");
            String end = scanner.nextLine().trim();

            SessionCreateRequest req = new SessionCreateRequest();
            req.setSectionSubjectId(ssId);
            req.setSectionId(sectionId);
            req.setDate(date);
            req.setStartTime(start);
            req.setEndTime(end);

            System.out.println(sessionController.createSession(req, loggedInUser.getId()));
        } catch (Exception e) {
            System.out.println("[!] Error: " + e.getMessage());
        }
    }

    private static void handleMarkAttendance(Scanner scanner) {
        try {
            System.out.print("Class Session ID: ");
            long sessionId = Long.parseLong(scanner.nextLine().trim());

            ClassSession session = classSessionService.findById(sessionId);
            System.out.println("Session: " + session.getSessionDate() + " " +
                    session.getStartTime() + "-" + session.getEndTime() + " [" + session.getStatus() + "]");

            // Load eligible students
            List<StudentSectionEnrollment> roster = enrollmentRepository
                    .findActiveEnrollmentsBySectionIdOnDate(session.getSectionId(), session.getSessionDate());

            if (roster.isEmpty()) {
                System.out.println("[!] No eligible students for this session.");
                return;
            }

            System.out.println("\n--- Eligible Students ---");
            System.out.println("Enter P for PRESENT, A for ABSENT");
            List<AttendanceMarkRequest> entries = new ArrayList<>();

            for (StudentSectionEnrollment enrollment : roster) {
                User student = userRepository.findById(enrollment.getStudentId()).orElse(null);
                String name = student != null ? student.getFullName() : "Unknown";
                String roll = student != null ? student.getUsername() : "";

                System.out.print("  " + roll + " (" + name + ") [P/A]: ");
                String input = scanner.nextLine().trim().toUpperCase();
                String status = input.startsWith("P") ? "PRESENT" : "ABSENT";

                AttendanceMarkRequest req = new AttendanceMarkRequest();
                req.setClassSessionId(sessionId);
                req.setEnrollmentId(enrollment.getId());
                req.setStudentId(enrollment.getStudentId());
                req.setStatus(status);
                entries.add(req);
            }

            BulkAttendanceRequest bulkReq = new BulkAttendanceRequest();
            bulkReq.setClassSessionId(sessionId);
            bulkReq.setAttendanceEntries(entries);
            bulkReq.setMarkedBy(loggedInUser.getId());

            System.out.print("\nSave as (D)raft or (F)inalize? ");
            String saveChoice = scanner.nextLine().trim().toUpperCase();

            if (saveChoice.startsWith("F")) {
                System.out.println(attendanceController.saveDraft(bulkReq));
                System.out.println(attendanceController.finalizeAttendance(sessionId, loggedInUser.getId()));
            } else {
                System.out.println(attendanceController.saveDraft(bulkReq));
            }
        } catch (Exception e) {
            System.out.println("[!] Error: " + e.getMessage());
        }
    }

    private static void handleFinalizeAttendance(Scanner scanner) {
        System.out.print("Class Session ID to finalize: ");
        long sessionId = Long.parseLong(scanner.nextLine().trim());
        System.out.println(attendanceController.finalizeAttendance(sessionId, loggedInUser.getId()));
    }

    private static void handleSectionReport(Scanner scanner) {
        System.out.print("Section ID: ");
        long sectionId = Long.parseLong(scanner.nextLine().trim());
        ApiResponse resp = reportController.getSectionReport(sectionId);
        System.out.println(resp.getMessage());
        if (resp.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> report = (List<Map<String, Object>>) resp.getData();
            System.out.printf("  %-20s %-15s %s%n", "Student", "Roll No", "Overall %");
            System.out.println("  " + "-".repeat(50));
            for (Map<String, Object> entry : report) {
                System.out.printf("  %-20s %-15s %.2f%%%n",
                        entry.get("studentName"), entry.get("rollNumber"), entry.get("overallPercentage"));
            }
        }
    }

    private static void handleBelowThreshold(Scanner scanner) {
        System.out.print("Section ID: ");
        long sectionId = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Threshold (default 75): ");
        String threshStr = scanner.nextLine().trim();
        double threshold = threshStr.isEmpty() ? 75.0 : Double.parseDouble(threshStr);

        ApiResponse resp = reportController.getBelowThresholdReport(sectionId, threshold);
        System.out.println(resp.getMessage());
        if (resp.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> report = (List<Map<String, Object>>) resp.getData();
            if (report.isEmpty()) {
                System.out.println("  All students are above the threshold!");
            } else {
                for (Map<String, Object> entry : report) {
                    System.out.printf("  Student %s | Conducted: %d | Attended: %d | %.2f%%%n",
                            entry.get("studentId"), entry.get("classesConducted"),
                            entry.get("classesAttended"), entry.get("attendancePercentage"));
                }
            }
        }
    }

    private static void handleGenerateSessions(Scanner scanner) {
        System.out.print("Section ID: ");
        long sectionId = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Days ahead (default 30): ");
        String daysStr = scanner.nextLine().trim();
        int days = daysStr.isEmpty() ? 30 : Integer.parseInt(daysStr);

        System.out.println(timetableController.generateSessions(sectionId, days));
    }

    private static void handleViewTimetable(Scanner scanner) {
        System.out.print("Section ID: ");
        long sectionId = Long.parseLong(scanner.nextLine().trim());
        ApiResponse resp = timetableController.getTimetable(sectionId);
        System.out.println(resp.getMessage());
        if (resp.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<WeeklyTimetable> entries = (List<WeeklyTimetable>) resp.getData();
            for (WeeklyTimetable entry : entries) {
                Subject subj = null;
                SectionSubject ss = sectionSubjectRepository.findById(entry.getSectionSubjectId()).orElse(null);
                if (ss != null) subj = subjectRepository.findById(ss.getSubjectId()).orElse(null);
                System.out.printf("  %-10s | %-30s | %s - %s%n",
                        entry.getDayOfWeek(),
                        subj != null ? subj.getSubjectName() : "Unknown",
                        entry.getStartTime(), entry.getEndTime());
            }
        }
    }

    private static void handleCancelSession(Scanner scanner) {
        System.out.print("Session ID to cancel: ");
        long sessionId = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Reason: ");
        String reason = scanner.nextLine().trim();
        System.out.println(sessionController.cancelSession(sessionId, reason));
    }

    // ===================== STUDENT MENU =====================

    private static void showStudentMenu(Scanner scanner) {
        System.out.println("\n--- STUDENT MENU (" + loggedInUser.getFullName() + ") ---");
        System.out.println("1. View My Attendance Dashboard");
        System.out.println("2. View Attendance Prediction");
        System.out.println("0. Logout");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": handleStudentDashboard(); break;
            case "2": handleStudentPrediction(scanner); break;
            case "0": loggedInUser = null; System.out.println("Logged out.\n"); break;
            default: System.out.println("[!] Invalid choice.");
        }
    }

    @SuppressWarnings("unchecked")
    private static void handleStudentDashboard() {
        ApiResponse resp = reportController.getStudentDashboard(loggedInUser.getId());
        if (!resp.isSuccess()) {
            System.out.println(resp);
            return;
        }

        Map<String, Object> dashboard = (Map<String, Object>) resp.getData();
        System.out.println("\n========== MY ATTENDANCE DASHBOARD ==========");
        System.out.println("Student : " + dashboard.get("studentName"));
        System.out.println("Roll No : " + dashboard.get("rollNumber"));
        System.out.println("Section : " + dashboard.get("sectionName"));
        System.out.printf("Overall : %.2f%%%n", (double) dashboard.get("overallPercentage"));
        System.out.println("----------------------------------------------");

        List<Map<String, Object>> subjects = (List<Map<String, Object>>) dashboard.get("subjectWiseAttendance");
        if (subjects != null) {
            System.out.printf("  %-30s %s  %s  %s  %s%n", "Subject", "Cond", "Att", "Miss", "Percentage");
            for (Map<String, Object> s : subjects) {
                System.out.printf("  %-30s %4d  %4d  %4d  %6.2f%%%n",
                        s.get("subjectName"), s.get("classesConducted"),
                        s.get("classesAttended"), s.get("classesMissed"),
                        s.get("attendancePercentage"));
            }
        }
        System.out.println("==============================================");
    }

    private static void handleStudentPrediction(Scanner scanner) {
        try {
            StudentSectionEnrollment enrollment = enrollmentRepository
                    .findActiveByStudentId(loggedInUser.getId()).orElse(null);
            if (enrollment == null) {
                System.out.println("[!] No active enrollment found.");
                return;
            }

            List<SectionSubject> ssList = sectionSubjectRepository.findBySectionId(enrollment.getSectionId());
            System.out.println("\n--- Subjects ---");
            for (SectionSubject ss : ssList) {
                Subject subj = subjectRepository.findById(ss.getSubjectId()).orElse(null);
                System.out.println("  [" + ss.getId() + "] " + (subj != null ? subj.getSubjectName() : "Unknown"));
            }

            System.out.print("Section-Subject ID: ");
            long ssId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Days ahead (default 30): ");
            String daysStr = scanner.nextLine().trim();
            int days = daysStr.isEmpty() ? 30 : Integer.parseInt(daysStr);

            ApiResponse resp = predictionController.predictAttendance(loggedInUser.getId(), ssId, days);
            System.out.println(resp.getMessage());
            if (resp.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> pred = (Map<String, Object>) resp.getData();
                System.out.println("\n--- Prediction ---");
                System.out.println("  Current : " + pred.get("currentAttended") + "/" + pred.get("currentConducted")
                        + " (" + pred.get("currentPercentage") + "%)");
                System.out.println("  Upcoming: " + pred.get("upcomingSessions") + " sessions");
                System.out.println("  If all attend: " + pred.get("predictedIfAllAttend") + "%");
                System.out.println("  If all absent: " + pred.get("predictedIfAllAbsent") + "%");
            }
        } catch (Exception e) {
            System.out.println("[!] Error: " + e.getMessage());
        }
    }
}
