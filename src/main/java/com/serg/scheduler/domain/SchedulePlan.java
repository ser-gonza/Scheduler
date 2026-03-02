package com.serg.scheduler.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//Represents current schedule being built in memory, but not yet saved. Exists in the user session

@Data
public class SchedulePlan {
    private final List<Course> courses = new ArrayList<>(); //List of selected course objects

    public void addCourse(Course course) {
        boolean presence = courses.stream().anyMatch(c -> c.getId().equals(course.getId())); //Adds course to plan if its not already there, is course already in the plan?
        if (!presence) courses.add(course); //Only add if not present
    }

    public void removeCourse(Long courseId) {
        courses.removeIf(c -> c.getId().equals(courseId)); //Remove any course who has an ID that matches the given courseId
    }

    public void reset() {
        courses.clear(); //Removes all courses from plan
    }
}
