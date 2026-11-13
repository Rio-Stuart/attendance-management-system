package com.attendance.repository;

import com.attendance.model.Section;

import java.util.List;
import java.util.Optional;

public interface SectionRepository {

    Section save(Section section);

    Optional<Section> findById(long id);

    Optional<Section> findBySectionName(String sectionName);

    List<Section> findAll();

    List<Section> findByAcademicYear(String academicYear);

    Section update(Section section);

    void delete(long id);
}
