package com.attendance.repository;

import com.attendance.model.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository {

    Subject save(Subject subject);

    Optional<Subject> findById(long id);

    Optional<Subject> findBySubjectCode(String subjectCode);

    List<Subject> findAll();

    Subject update(Subject subject);

    void delete(long id);
}
