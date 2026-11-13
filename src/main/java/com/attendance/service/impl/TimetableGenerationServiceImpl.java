package com.attendance.service.impl;

import com.attendance.model.ClassSession;
import com.attendance.model.SectionSubject;
import com.attendance.model.WeeklyTimetable;
import com.attendance.model.enums.SessionStatus;
import com.attendance.repository.ClassSessionRepository;
import com.attendance.repository.HolidayRepository;
import com.attendance.repository.SectionSubjectRepository;
import com.attendance.repository.TimetableRepository;
import com.attendance.service.TimetableGenerationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TimetableGenerationServiceImpl implements TimetableGenerationService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final TimetableRepository timetableRepository;
    private final ClassSessionRepository classSessionRepository;
    private final HolidayRepository holidayRepository;
    private final SectionSubjectRepository sectionSubjectRepository;

    public TimetableGenerationServiceImpl(TimetableRepository timetableRepository,
                                           ClassSessionRepository classSessionRepository,
                                           HolidayRepository holidayRepository,
                                           SectionSubjectRepository sectionSubjectRepository) {
        this.timetableRepository = timetableRepository;
        this.classSessionRepository = classSessionRepository;
        this.holidayRepository = holidayRepository;
        this.sectionSubjectRepository = sectionSubjectRepository;
    }

    @Override
    public List<ClassSession> generateSessionsForDateRange(long sectionId, LocalDate from, LocalDate to) {
        List<ClassSession> generated = new ArrayList<>();

        // Get all timetable entries for this section
        List<SectionSubject> sectionSubjects = sectionSubjectRepository.findBySectionId(sectionId);

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            // Skip holidays
            if (holidayRepository.isHoliday(date)) continue;

            // Find valid timetable entries for this date's day of week
            for (SectionSubject ss : sectionSubjects) {
                List<WeeklyTimetable> entries = timetableRepository.findBySectionSubjectId(ss.getId());

                for (WeeklyTimetable entry : entries) {
                    // Check day of week matches
                    if (entry.getDayOfWeek() != date.getDayOfWeek()) continue;

                    // Check validity period
                    if (entry.getValidFrom() != null && date.isBefore(entry.getValidFrom())) continue;
                    if (entry.getValidTo() != null && date.isAfter(entry.getValidTo())) continue;

                    // Idempotent: skip if session already exists
                    if (classSessionRepository.existsByUnique(ss.getId(), date, entry.getStartTime())) continue;

                    // Create SCHEDULED session
                    ClassSession session = new ClassSession();
                    session.setSectionSubjectId(ss.getId());
                    session.setSectionId(sectionId);
                    session.setTimetableEntryId(entry.getId());
                    session.setSessionDate(date);
                    session.setStartTime(entry.getStartTime());
                    session.setEndTime(entry.getEndTime());
                    session.setStatus(SessionStatus.SCHEDULED);
                    session.setCreatedAt(LocalDateTime.now(IST));

                    ClassSession saved = classSessionRepository.save(session);
                    generated.add(saved);
                }
            }
        }

        return generated;
    }

    @Override
    public List<ClassSession> generateSessionsForSection(long sectionId, int daysAhead) {
        LocalDate from = LocalDate.now(IST);
        LocalDate to = from.plusDays(daysAhead);
        return generateSessionsForDateRange(sectionId, from, to);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.isHoliday(date);
    }
}
