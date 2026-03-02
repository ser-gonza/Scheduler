package com.serg.scheduler.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Permanent saved schedule record

//Stored in DB table
@Entity
@Data
public class ScheduleSubmission {
    //DB creates IDs for each submission
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt = LocalDateTime.now();

    @NotEmpty(message = "Choose at the minimum one course before submitting.")
    @ManyToMany //Relationship between ScheduleSubmission and Course, allows each submission to have multiple courses and each course to belong to multiple entries.
    private List<Course> courses = new ArrayList<>();
}
