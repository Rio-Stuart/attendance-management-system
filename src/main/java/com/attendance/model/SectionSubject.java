package com.attendance.model;

public class SectionSubject {

    private long id;
    private long sectionId;
    private long subjectId;

    public SectionSubject() {}

    public SectionSubject(long id, long sectionId, long subjectId) {
        this.id = id;
        this.sectionId = sectionId;
        this.subjectId = subjectId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSectionId() { return sectionId; }
    public void setSectionId(long sectionId) { this.sectionId = sectionId; }

    public long getSubjectId() { return subjectId; }
    public void setSubjectId(long subjectId) { this.subjectId = subjectId; }

    @Override
    public String toString() {
        return "SectionSubject{" +
                "id=" + id +
                ", sectionId=" + sectionId +
                ", subjectId=" + subjectId +
                '}';
    }
}
