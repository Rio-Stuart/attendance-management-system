# Attendance Management System (AMS)

A full-stack, enterprise-grade university attendance management platform implementing the **Mandatory Class Sessions & Attendance Records Architecture**.

Built with a dual-engine architecture:
1. **Full Java 26 / JDBC Backend** with H2 in-memory/embedded database, atomic transaction finalization, and 20 automated unit/integration tests.
2. **Modern Web Application & Netlify Serverless API** with responsive dashboard, attendance marking sheet, timetable generator, and interactive attendance predictor.

---

## 🌟 Architectural Highlights

- **Mandatory Invariant**: An attendance record cannot exist independently of a class session.
- **Relational Integrity**:
  - `class_sessions`: Authoritative entity for individual lecture occurrences (`SCHEDULED`, `DRAFT`, `FINALIZED`, `CANCELLED`). Unique constraint on `(section_subject_id, session_date, start_time)`.
  - `attendance_records`: Individual student attendance per session (`PRESENT`, `ABSENT`). Unique constraint on `(class_session_id, student_section_enrollment_id)`.
  - `student_section_enrollments`: Historical section transfer tracker.
- **Atomic Transactional Finalization**: Verifies that 100% of the active student roster for that section on that date has been marked, rejecting partial records and foreign students.
- **Strict Calculations**:
  - Excludes `SCHEDULED`, `DRAFT`, and `CANCELLED` sessions.
  - Overall attendance is strictly weighted:
    $$\text{Overall \%} = \left(\frac{\sum P}{\sum T}\right) \times 100 \quad (\text{not arithmetic mean})$$
- **Idempotent Timetable Generator**: Generates individual lecture sessions from recurring weekly templates while automatically skipping university holidays.
- **Attendance Predictor**: Scenario simulator calculating future attendance projections ($P, T, K, X$).

---

## 🚀 Quick Start

### 1. Web Application (Local Server)
```bash
npm install
npm start
```
Navigate to `http://localhost:3000`.

### 2. Java Course Project
```bash
# Compile and run console application
.\build.bat

# Execute 20 automated test cases
.\test.bat
```

---

## 👥 Demo Credentials

| Role | Username | Password | Notes |
|---|---|---|---|
| **Admin** | `admin1` | `admin123` | Dr. Rajesh Kumar |
| **Admin** | `admin2` | `admin123` | Prof. Anita Sharma |
| **Student** | `student1` | `student123` | Aarav Patel (Section 25BCS801) |
| **Student** | `student4` | `student123` | Sneha Reddy (Section 25BCS802) |

---

## 📂 Repository Structure

```
├── lib/                     # H2 Database JDBC Driver JAR
├── sql/                     # DDL Schema & Initial Seed Data
│   ├── schema.sql
│   └── seed.sql
├── src/                     # Java Backend & Tests
│   ├── main/java/com/attendance/
│   │   ├── config/          # DatabaseConfig singleton
│   │   ├── controller/      # API / Console Controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Custom Exception Hierarchy
│   │   ├── model/           # Relational Models & Enums
│   │   ├── repository/      # JDBC Repository Implementations
│   │   ├── service/         # Business Logic & Transactions
│   │   └── util/            # Date & Time Utilities
│   └── test/java/com/attendance/ # 20 Mandatory Test Cases
├── netlify/
│   └── functions/api.mjs    # Serverless Netlify API Handler
├── public/                  # Responsive Single-Page Web Portal
│   ├── index.html
│   ├── styles.css
│   └── app.js
├── db.js                    # In-Memory Relational Engine for Web
├── server.js                # Express REST API Server
├── netlify.toml             # Netlify Deployment Configuration
├── compile.bat              # Java Compilation Script
├── build.bat                # Java Console App Launcher
└── test.bat                 # Java Test Runner
```

---

## 📄 License
MIT License. Created for University Coursework.
