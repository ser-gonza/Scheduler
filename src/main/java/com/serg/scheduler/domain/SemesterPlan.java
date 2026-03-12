package com.serg.scheduler.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SemesterPlan {

    private String name;
    private List<Course> courses = new ArrayList<>(); //List of courses in that semester

    public SemesterPlan(String name) {
        this.name = name;
    }

    public int getTotalCredits() {
        return courses.stream().mapToInt(Course::getCredits).sum();
    }
}