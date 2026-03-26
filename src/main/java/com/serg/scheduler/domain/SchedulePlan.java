package com.serg.scheduler.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SchedulePlan {

    //Fields
    private String username;
    private String expectedGraduationDate;
    private Major selectedMajor;

    //Role, completion, and status tracking
    private String userRole = "STUDENT";
    private List<Long> completedCourseIds = new ArrayList<>();
    private Map<Long, String> courseStatuses = new HashMap<>();
    private static final int DEFAULT_CREDITS_PER_SEMESTER = 15;

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

            //Assign add/waitlist status
            courseStatuses.put(course.getId(), determineCourseStatus(course));
        }
    }

    //Loops through all semesters and removes course whenever it appears
    public void removeCourse(Long courseId) {
        for (SemesterPlan semester : semesters) {
            semester.getCourses().removeIf(c -> c.getId().equals(courseId));
        }

        //Clean up removed course metadata
        courseStatuses.remove(courseId);
        completedCourseIds.remove(courseId);
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

        //Clear new session state
        completedCourseIds.clear();
        courseStatuses.clear();
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
        return hasCompletedCourseCodeBeforeSemester(courseCode, currentSemesterIndex, null);
    }

    //Reads prereq text from course and checks whether all listed prereqs have been completed
    public boolean canTakeCourse(Course course) {
        return canTakeCourseInSemester(course, currentSemesterIndex);
    }

    //Builds list of which prereqs are still missing
    public String getMissingPrerequisites(Course course) {
        return getMissingPrerequisitesForSemester(course, currentSemesterIndex);
    }

    //Helpers for role based access
    public boolean canModifySchedule() {
        return "STUDENT".equalsIgnoreCase(userRole);
    }

    public boolean canSubmitSchedule() {
        return "STUDENT".equalsIgnoreCase(userRole);
    }

    //Helpers for professors/class size and queue state
    public String determineCourseStatus(Course course) {
        if (course.getMaxCapacity() != null && course.getEnrolledCount() != null
                && course.getMaxCapacity() > 0
                && course.getEnrolledCount() >= course.getMaxCapacity()) {
            return "Waitlisted";
        }
        return "Added";
    }

    public String getCourseStatus(Long courseId) {
        return courseStatuses.getOrDefault(courseId, "Not Added");
    }

    public void setCourseStatus(Long courseId, String status) {
        courseStatuses.put(courseId, status);
    }

    //Helpers for move course feature
    public boolean moveCourseToSemester(Long courseId, int targetSemesterIndex) {
        if (targetSemesterIndex < 0 || targetSemesterIndex >= semesters.size()) {
            return false;
        }

        Course courseToMove = null;
        int sourceSemesterIndex = -1;

        for (int i = 0; i < semesters.size(); i++) {
            for (Course course : semesters.get(i).getCourses()) {
                if (course.getId().equals(courseId)) {
                    courseToMove = course;
                    sourceSemesterIndex = i;
                    break;
                }
            }
            if (courseToMove != null) {
                break;
            }
        }

        if (courseToMove == null || sourceSemesterIndex == targetSemesterIndex) {
            return false;
        }

        semesters.get(sourceSemesterIndex).getCourses().removeIf(c -> c.getId().equals(courseId));

        if (!canTakeCourseInSemester(courseToMove, targetSemesterIndex)) {
            semesters.get(sourceSemesterIndex).getCourses().add(courseToMove);
            return false;
        }

        semesters.get(targetSemesterIndex).getCourses().add(courseToMove);
        return true;
    }

    //Helpers for completed course tracking
    public void markCourseCompleted(Long courseId) {
        if (isCourseSelected(courseId) && !completedCourseIds.contains(courseId)) {
            completedCourseIds.add(courseId);
        }
    }

    public void markCourseIncomplete(Long courseId) {
        completedCourseIds.remove(courseId);
    }

    public boolean isCourseCompleted(Long courseId) {
        return completedCourseIds.contains(courseId);
    }

    //Helpers for major specific electives
    public boolean isCourseRelevantForSelectedMajor(Course course) {
        if (selectedMajor == null) {
            return true;
        }

        if (course.getRequirementType() == null || !course.getRequirementType().equalsIgnoreCase("Elective")) {
            return true;
        }

        String recommendedMajorNames = course.getRecommendedMajorNames();
        if (recommendedMajorNames == null || recommendedMajorNames.isBlank() || recommendedMajorNames.equalsIgnoreCase("All")) {
            return true;
        }

        List<String> allowedMajors = Arrays.stream(recommendedMajorNames.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        return allowedMajors.stream().anyMatch(name -> name.equalsIgnoreCase(selectedMajor.getName()));
    }

    public String getMajorFitLabel(Course course) {
        return isCourseRelevantForSelectedMajor(course) ? "Fits Major" : "Not Recommended";
    }

    //Helpers for graduation progress warnings
    public boolean courseHelpsProgress(Course course) {
        if (selectedMajor == null) {
            return true;
        }

        if (!isCourseRelevantForSelectedMajor(course)) {
            return false;
        }

        if (course.getCreditCategory() != null) {
            if (course.getCreditCategory().equalsIgnoreCase("Arts & Humanities")
                    && getCreditsForCategory("Arts & Humanities") < selectedMajor.getArtsHumanitiesRequired()) {
                return true;
            }

            if (course.getCreditCategory().equalsIgnoreCase("Math & Science")
                    && getCreditsForCategory("Math & Science") < selectedMajor.getMathScienceRequired()) {
                return true;
            }

            if (course.getCreditCategory().equalsIgnoreCase("Major")
                    && getCreditsForCategory("Major") < selectedMajor.getMajorCreditsRequired()) {
                return true;
            }
        }

        return getTotalCredits() < selectedMajor.getTotalCreditsRequired();
    }

    public int getRemainingCreditsToGraduate() {
        if (selectedMajor == null) {
            return 0;
        }
        return Math.max(selectedMajor.getTotalCreditsRequired() - getTotalCredits(), 0);
    }

    public int getSemestersRemainingIncludingCurrent() {
        return semesters.size() - currentSemesterIndex;
    }

    public boolean isBehindExpectedGraduation() {
        return getRemainingCreditsToGraduate() > (getSemestersRemainingIncludingCurrent() * DEFAULT_CREDITS_PER_SEMESTER);
    }

    public String getGraduationAlert() {
        if (!isBehindExpectedGraduation()) {
            return "";
        }

        return "Warning: At your current pace, you may fall behind your expected graduation date of "
                + expectedGraduationDate + ".";
    }

    //Helpers for prerequisite checks across any semester
    public boolean canTakeCourseInSemester(Course course, int semesterIndex) {
        String prereqText = course.getPrerequisites();

        if (prereqText == null || prereqText.isBlank() || prereqText.equalsIgnoreCase("None")) {
            return true;
        }

        List<String> prereqs = Arrays.stream(prereqText.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        for (String prereq : prereqs) {
            if (!hasCompletedCourseCodeBeforeSemester(prereq, semesterIndex, course.getId())) {
                return false;
            }
        }

        return true;
    }

    public String getMissingPrerequisitesForSemester(Course course, int semesterIndex) {
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
            if (!hasCompletedCourseCodeBeforeSemester(prereq, semesterIndex, course.getId())) {
                missing.add(prereq);
            }
        }

        return String.join(", ", missing);
    }

    private boolean hasCompletedCourseCodeBeforeSemester(String courseCode, int semesterIndex, Long excludedCourseId) {
        for (Long completedCourseId : completedCourseIds) {
            Course completedCourse = findCourseById(completedCourseId);
            if (completedCourse != null && completedCourse.getCode().equalsIgnoreCase(courseCode.trim())) {
                return true;
            }
        }

        for (int i = 0; i < semesterIndex; i++) {
            for (Course course : semesters.get(i).getCourses()) {
                boolean isExcludedCourse = excludedCourseId != null && course.getId().equals(excludedCourseId);
                if (!isExcludedCourse && course.getCode().equalsIgnoreCase(courseCode.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    private Course findCourseById(Long courseId) {
        return semesters.stream()
                .flatMap(semester -> semester.getCourses().stream())
                .filter(course -> course.getId().equals(courseId))
                .findFirst()
                .orElse(null);
    }

    //Persistence helpers for saved schedules
    public String getCompletedCourseIdsCsv() {
        return completedCourseIds.stream()
                .map(String::valueOf)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    public void loadCompletedCourseIdsFromCsv(String csv) {
        completedCourseIds.clear();

        if (csv == null || csv.isBlank()) {
            return;
        }

        Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .forEach(completedCourseIds::add);
    }

    public String buildCourseStatusesCsvForSemester(SemesterPlan semesterPlan) {
        List<String> entries = new ArrayList<>();

        for (Course course : semesterPlan.getCourses()) {
            String status = getCourseStatus(course.getId());
            entries.add(course.getId() + ":" + status);
        }

        return String.join("|", entries);
    }

    public void loadCourseStatusesFromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return;
        }

        String[] entries = csv.split("\\|");
        for (String entry : entries) {
            if (entry.isBlank() || !entry.contains(":")) {
                continue;
            }

            String[] parts = entry.split(":", 2);
            Long courseId = Long.valueOf(parts[0].trim());
            String status = parts[1].trim();
            courseStatuses.put(courseId, status);
        }
    }
}