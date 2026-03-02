package com.serg.scheduler.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

//Course stored in the database

@Entity //JPA maps class to DB table
@Data
public class Course {

    @Id //Key for the table
    private Long id;

    //Columns
    private String code;
    private String title;
    private int credits;
    private String timeslot;
}
