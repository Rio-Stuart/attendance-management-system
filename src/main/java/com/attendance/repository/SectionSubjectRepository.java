package com.attendance.repository;

import com.attendance.model.SectionSubject;

import java.util.List;
import java.util.Optional;

public interface SectionSubjectRepository {

    SectionSubject save(SectionSubject sectionSubject);

    Optional<SectionSubject> findById(long id);

    List<SectionSubject> findBySectionId(long sectionId);

    List<SectionSubject> findBySubjectId(long subjectId);

    Optional<SectionSubject> findBySectionAndSubject(long sectionId, long subjectId);

    List<SectionSubject> findAll();

    void delete(long id);
}
