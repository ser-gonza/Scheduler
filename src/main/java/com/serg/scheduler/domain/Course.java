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

    //New fields
    private String creditCategory;   //Arts & Humanities, Math & Science, Major
    private String requirementType;  //Core, Elective, GenEd
    private String prerequisites;    //Simple text for now

    //Course availability and major-specific scheduling info
    private String professorName;
    private Integer maxCapacity;
    private Integer enrolledCount;
    private String recommendedMajorNames; //Comma separated majors or All
}