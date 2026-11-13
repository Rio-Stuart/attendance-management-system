@echo off
echo ========================================
echo   Compiling Attendance Management System
echo ========================================
if not exist out mkdir out

javac -cp "lib/*" -d out -sourcepath src/main/java src/main/java/com/attendance/AttendanceManagementApp.java 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    exit /b 1
)
echo [OK] Compilation successful.
