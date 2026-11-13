package com.attendance.repository;

import com.attendance.model.AttendanceAuditLog;

import java.util.List;

public interface AttendanceAuditRepository {

    AttendanceAuditLog save(AttendanceAuditLog auditLog);

    List<AttendanceAuditLog> findByRecordId(long attendanceRecordId);
}
