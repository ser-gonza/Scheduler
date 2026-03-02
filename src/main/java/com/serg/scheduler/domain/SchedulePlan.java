package com.serg.scheduler.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//Represents current schedule being built in memory, but not yet saved. Exists in the user session

@Data
public class SchedulePlan {
    private final List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        boolean exists = courses.stream().anyMatch(c -> c.getId().equals(course.getId()));
        if (!exists) courses.add(course);
    }

    public void removeCourse(Long courseId) {
        courses.removeIf(c -> c.getId().equals(courseId));
    }

    public void reset() {
        courses.clear();
    }
}
