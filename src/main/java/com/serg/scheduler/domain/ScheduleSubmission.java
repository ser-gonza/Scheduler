package com.serg.scheduler.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Permanent saved schedule record

//Stored in DB table
@Entity
@Data //Lombok, adds helper methods
public class ScheduleSubmission {
    //DB creates IDs for each submission
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expectedGraduationDate;
    private String username;
    private String majorName;
    private int totalCredits;

    //Persisted completion state
    private String completedCourseIdsCsv;
    //Persisted completion state

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("semesterIndex ASC")
    private List<SubmittedSemester> semesters = new ArrayList<>();
}