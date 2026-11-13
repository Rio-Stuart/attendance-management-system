package com.attendance.service;

import com.attendance.model.AttendanceRecord;
import com.attendance.model.enums.AttendanceStatus;

import java.util.List;
import java.util.Map;

public interface AttendanceService {

    void markAttendance(long classSessionId, long studentId, long enrollmentId,
                        AttendanceStatus status, long markedBy);

    void markBulkAttendance(long classSessionId, Map<Long, AttendanceStatus> enrollmentStatusMap,
                            long markedBy);

    void saveDraft(long classSessionId, Map<Long, AttendanceStatus> enrollmentStatusMap,
                   long markedBy);

    void finalizeAttendance(long classSessionId, long finalizedBy);

    void editFinalizedAttendance(long attendanceRecordId, AttendanceStatus newStatus,
                                 long changedBy, String reason);

    List<AttendanceRecord> getSessionAttendance(long classSessionId);
}
