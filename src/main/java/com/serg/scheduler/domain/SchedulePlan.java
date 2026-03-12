package com.serg.scheduler.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class SchedulePlan {

    //Fields
    private String username;
    private String expectedGraduationDate;
    private Major selectedMajor;

    private List<SemesterPlan> semesters = new ArrayList<>();
    private int currentSemesterIndex = 0;

    //Constructor
    public SchedulePlan() {
        semesters.add(new SemesterPlan("Semester 1"));
        semesters.add(new SemesterPlan("Semester 2"));
        semesters.add(new SemesterPlan("Semester 3"));
        semesters.add(new SemesterPlan("Semester 4"));
        semesters.add(new SemesterPlan("Semester 5"));
        semesters.add(new SemesterPlan("Semester 6"));
        semesters.add(new SemesterPlan("Semester 7"));
        semesters.add(new SemesterPlan("Semester 8"));
    }

    //Returns semester currently being edited
    public SemesterPlan getCurrentSemester() {
        return semesters.get(currentSemesterIndex);
    }

    public String getCurrentSemesterName() {
        return getCurrentSemester().getName();
    }

    //Prevents duplicates, checks prereqs, add current course to semester
    public void addCourse(Course course) {
        if (!isCourseSelected(course.getId()) && canTakeCourse(course)) {
            getCurrentSemester().getCourses().add(course);
        }
    }

    //Loops through all semesters and removes course whenever it appears
    public void removeCourse(Long courseId) {
        for (SemesterPlan semester : semesters) {
            semester.getCourses().removeIf(c -> c.getId().equals(courseId));
        }
    }

    //Clears semester list
    public void reset() {
        semesters.clear();
        semesters.add(new SemesterPlan("Semester 1"));
        semesters.add(new SemesterPlan("Semester 2"));
        semesters.add(new SemesterPlan("Semester 3"));
        semesters.add(new SemesterPlan("Semester 4"));
        semesters.add(new SemesterPlan("Semester 5"));
        semesters.add(new SemesterPlan("Semester 6"));
        semesters.add(new SemesterPlan("Semester 7"));
        semesters.add(new SemesterPlan("Semester 8"));
        currentSemesterIndex = 0;
    }

    //Change which semester user is actively editing
    public void nextSemester() {
        if (currentSemesterIndex < 7) {
            currentSemesterIndex++;
        }
    }

    public void previousSemester() {
        if (currentSemesterIndex > 0) {
            currentSemesterIndex--;
        }
    }

    //Checks whether course has been selected
    public boolean isCourseSelected(Long courseId) {
        return semesters.stream()
                .flatMap(s -> s.getCourses().stream())
                .anyMatch(c -> c.getId().equals(courseId));
    }

    //Adds total credits
    public int getTotalCredits() {
        return semesters.stream().mapToInt(SemesterPlan::getTotalCredits).sum();
    }

    //Filters courses (Arts & Humanities, Math & Science, Major)
    public int getCreditsForCategory(String category) {
        return semesters.stream()
                .flatMap(s -> s.getCourses().stream())
                .filter(c -> category.equalsIgnoreCase(c.getCreditCategory()))
                .mapToInt(Course::getCredits)
                .sum();
    }

    //Returns all courses
    public List<Course> getAllCourses() {
        return semesters.stream()
                .flatMap(s -> s.getCourses().stream())
                .toList();
    }

    //Collects all courses from semesters before the active one
    public List<Course> getCompletedCoursesBeforeCurrentSemester() {
        List<Course> completed = new ArrayList<>();

        for (int i = 0; i < currentSemesterIndex; i++) {
            completed.addAll(semesters.get(i).getCourses());
        }

        return completed;
    }

    //Checks if prereq has already been completed
    public boolean hasCompletedCourseCode(String courseCode) {
        return getCompletedCoursesBeforeCurrentSemester().stream()
                .anyMatch(c -> c.getCode().equalsIgnoreCase(courseCode.trim()));
    }

    //Reads prereq text from course and checks whether all listed prereqs have been completed
    public boolean canTakeCourse(Course course) {
        String prereqText = course.getPrerequisites();

        if (prereqText == null || prereqText.isBlank() || prereqText.equalsIgnoreCase("None")) {
            return true;
        }

        //Supports simple comma-separated prereqs like "CSCI-C211, MATH-M119"
        List<String> prereqs = Arrays.stream(prereqText.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        for (String prereq : prereqs) {
            if (!hasCompletedCourseCode(prereq)) {
                return false;
            }
        }

        return true;
    }

    //Builds list of which prereqs are still missing
    public String getMissingPrerequisites(Course course) {
        String prereqText = course.getPrerequisites();

        if (prereqText == null || prereqText.isBlank() || prereqText.equalsIgnoreCase("None")) {
            return "";
        }

        List<String> prereqs = Arrays.stream(prereqText.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        List<String> missing = new ArrayList<>();

        for (String prereq : prereqs) {
            if (!hasCompletedCourseCode(prereq)) {
                missing.add(prereq);
            }
        }

        return String.join(", ", missing);
    }
}