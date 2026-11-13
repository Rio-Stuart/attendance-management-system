@echo off
echo ========================================
echo   Building and Running Application
echo ========================================
if not exist out mkdir out

echo [*] Compiling...
javac -cp "lib/*" -d out -sourcepath src/main/java src/main/java/com/attendance/AttendanceManagementApp.java 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b 1
)
echo [OK] Compilation successful.
echo.
echo [*] Running application...
echo ========================================
java -cp "out;lib/*;." com.attendance.AttendanceManagementApp
