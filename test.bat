@echo off
echo ========================================
echo   Running Test Suite (20 Test Cases)
echo ========================================
if not exist out mkdir out

echo [*] Compiling application...
javac -cp "lib/*" -d out -sourcepath src/main/java src/main/java/com/attendance/AttendanceManagementApp.java 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Application compilation failed!
    exit /b 1
)

echo [*] Compiling test suite...
javac -cp "out;lib/*" -d out -sourcepath "src/test/java;src/main/java" src/test/java/com/attendance/AttendanceSystemTest.java 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Test compilation failed!
    exit /b 1
)

echo [*] Executing tests...
echo ========================================
java -cp "out;lib/*;." com.attendance.AttendanceSystemTest
