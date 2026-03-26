package com.serg.scheduler.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class SubmittedSemester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    //Represents one semester inside one saved submission
    private String name;

    private int semesterIndex; //Order*

    //Persisted queue/waitlist statuses
    @Column(length = 2000)
    private String courseStatusesCsv;
    //Persisted queue/waitlist statuses

    //Ties saved semester back to overall submitted plan
    @ManyToMany
    @JoinTable(
            name = "submitted_semester_courses",
            joinColumns = @JoinColumn(name = "submitted_semester_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private ScheduleSubmission submission;
}