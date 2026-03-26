package com.serg.scheduler.controller;

import com.serg.scheduler.domain.SubmittedSemester;
import com.serg.scheduler.domain.SemesterPlan;
import com.serg.scheduler.data.CourseRepository;
import com.serg.scheduler.data.MajorRepository;
import com.serg.scheduler.data.ScheduleSubmissionRepository;
import com.serg.scheduler.domain.Course;
import com.serg.scheduler.domain.Major;
import com.serg.scheduler.domain.SchedulePlan;
import com.serg.scheduler.domain.ScheduleSubmission;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@SessionAttributes("schedulePlan")
public class PlannerController {

    //Constructor injection
    private final CourseRepository courseRepo;
    private final MajorRepository majorRepo;
    private final ScheduleSubmissionRepository submissionRepo;

    public PlannerController(CourseRepository courseRepo,
                             MajorRepository majorRepo,
                             ScheduleSubmissionRepository submissionRepo) {
        this.courseRepo = courseRepo;
        this.majorRepo = majorRepo;
        this.submissionRepo = submissionRepo;
    }

    //If session doesn't already have schedulePlan object, create one
    @ModelAttribute("schedulePlan")
    public SchedulePlan schedulePlan() {
        return new SchedulePlan();
    }

    //Loads all majors from database
    @GetMapping("/majors")
    public String majors(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                         Model model) {
        //View only roles should not enter major-selection flow
        if (!"STUDENT".equalsIgnoreCase(schedulePlan.getUserRole())) {
            return "redirect:/submitted-schedules";
        }

        model.addAttribute("majors", majorRepo.findAll());
        return "majors";
    }

    //Gets chosen major ID and finds major in database, stores in current session's schedulePlan, redirects to courses page
    @PostMapping("/majors/select")
    public String selectMajor(@RequestParam("majorId") Long majorId,
                              @ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        //Only students can choose and edit majors
        if (!"STUDENT".equalsIgnoreCase(schedulePlan.getUserRole())) {
            return "redirect:/submitted-schedules";
        }

        Major major = majorRepo.findById(majorId).orElse(null);
        if (major != null) {
            schedulePlan.setSelectedMajor(major);
        }
        return "redirect:/courses";
    }

    //Ensures user has logged in and picked a major
    @GetMapping("/courses")
    public String courses(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                          Model model) {
        if (schedulePlan.getUsername() == null || schedulePlan.getUsername().isBlank()) {
            return "redirect:/login";
        }

        //View only roles should only see submitted schedules, not the editing page
        if (!"STUDENT".equalsIgnoreCase(schedulePlan.getUserRole())) {
            return "redirect:/submitted-schedules";
        }

        if (schedulePlan.getSelectedMajor() == null) {
            return "redirect:/majors";
        }

        populateCoursesModel(schedulePlan, model);
        return "courses";
    }

    //Shows 8 semester schedule
    @GetMapping("/schedule")
    public String schedule(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                           Model model) {
        //Checks login because page is displaying current session plan
        if (schedulePlan.getUsername() == null || schedulePlan.getUsername().isBlank()) {
            return "redirect:/login";
        }

        //View-only roles should see submitted schedules page instead
        if (!"STUDENT".equalsIgnoreCase(schedulePlan.getUserRole())) {
            return "redirect:/submitted-schedules";
        }

        //Keep schedule page synced with live alerts
        model.addAttribute("graduationAlert", schedulePlan.getGraduationAlert());

        //Shows read only message for professor/dean
        model.addAttribute("isViewOnlyRole", !schedulePlan.canModifySchedule());

        return "schedule";
    }

    //Shows all submitted schedules for professor/dean
    @GetMapping("/submitted-schedules")
    public String submittedSchedules(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                                     Model model) {
        //Checks login because page is displaying protected records
        if (schedulePlan.getUsername() == null || schedulePlan.getUsername().isBlank()) {
            return "redirect:/login";
        }

        //Only view only roles should access submitted schedules page
        if ("STUDENT".equalsIgnoreCase(schedulePlan.getUserRole())) {
            return "redirect:/schedule";
        }

        List<ScheduleSubmission> submissions = new ArrayList<>();
        submissionRepo.findAll().forEach(submissions::add);

        submissions.sort(Comparator.comparing(ScheduleSubmission::getCreatedAt).reversed());

        model.addAttribute("submissions", submissions);
        return "submitted-schedules";
    }

    //Gets selected course ID, loads course from database, checks if prereqs are satisfied, if not shows error, if yes add course to current semester
    @PostMapping("/schedule/add")
    public String add(@RequestParam("courseId") Long courseId,
                      @ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                      Model model) {
        Course course = courseRepo.findById(courseId).orElse(null);

        //Role enforcement
        if (!schedulePlan.canModifySchedule()) {
            return "redirect:/submitted-schedules";
        }

        if (course != null) {
            if (!schedulePlan.canTakeCourse(course)) {
                populateCoursesModel(schedulePlan, model);
                model.addAttribute("addError",
                        "Cannot add " + course.getCode() + ". Missing prerequisite(s): "
                                + schedulePlan.getMissingPrerequisites(course));
                return "courses";
            }

            schedulePlan.addCourse(course);

            //Success and progress warnings
            populateCoursesModel(schedulePlan, model);
            model.addAttribute("addSuccess",
                    course.getCode() + " added with status: " + schedulePlan.getCourseStatus(course.getId()) + ".");

            if (!schedulePlan.courseHelpsProgress(course)) {
                model.addAttribute("addWarning",
                        course.getCode() + " does not currently help progress toward your selected degree requirements.");
            }
            return "courses";
        }

        return "redirect:/courses";
    }

    //Removes course
    @PostMapping("/schedule/remove")
    public String remove(@RequestParam("courseId") Long courseId,
                         @ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        if (!schedulePlan.canModifySchedule()) {
            return "redirect:/submitted-schedules";
        }

        schedulePlan.removeCourse(courseId);
        return "redirect:/courses";
    }

    //Resets plan to 8 empty semesters
    @PostMapping("/schedule/reset")
    public String reset(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        if (!schedulePlan.canModifySchedule()) {
            return "redirect:/submitted-schedules";
        }

        schedulePlan.reset();
        return "redirect:/courses";
    }

    //Move active semester forward/backward
    @PostMapping("/schedule/next-semester")
    public String nextSemester(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        if (!schedulePlan.canModifySchedule()) {
            return "redirect:/submitted-schedules";
        }

        schedulePlan.nextSemester();
        return "redirect:/courses";
    }

    //Move active semester forward/backward
    @PostMapping("/schedule/previous-semester")
    public String previousSemester(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        if (!schedulePlan.canModifySchedule()) {
            return "redirect:/submitted-schedules";
        }

        schedulePlan.previousSemester();
        return "redirect:/courses";
    }

    //Move course between semesters
    @PostMapping("/schedule/move")
    public String move(@RequestParam("courseId") Long courseId,
                       @RequestParam("targetSemesterIndex") int targetSemesterIndex,
                       @ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                       Model model) {
        if (!schedulePlan.canModifySchedule()) {
            return "redirect:/submitted-schedules";
        }

        boolean moved = schedulePlan.moveCourseToSemester(courseId, targetSemesterIndex);
        if (!moved) {
            model.addAttribute("moveError", "Course could not be moved because the target semester would violate prerequisite order.");
            model.addAttribute("graduationAlert", schedulePlan.getGraduationAlert());
            model.addAttribute("isViewOnlyRole", !schedulePlan.canModifySchedule());
            return "schedule";
        }

        return "redirect:/schedule";
    }

    //Mark course completed or incomplete
    @PostMapping("/schedule/mark-completed")
    public String markCompleted(@RequestParam("courseId") Long courseId,
                                @ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        if (!schedulePlan.canModifySchedule()) {
            return "redirect:/submitted-schedules";
        }

        schedulePlan.markCourseCompleted(courseId);
        return "redirect:/schedule";
    }

    @PostMapping("/schedule/mark-incomplete")
    public String markIncomplete(@RequestParam("courseId") Long courseId,
                                 @ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        if (!schedulePlan.canModifySchedule()) {
            return "redirect:/submitted-schedules";
        }

        schedulePlan.markCourseIncomplete(courseId);
        return "redirect:/schedule";
    }

    //Submits the plan, makes sure it is not empty,
    @PostMapping("/schedule/submit")
    public String submit(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                         SessionStatus status,
                         Model model) {

        //Role enforcement on submit
        if (!schedulePlan.canSubmitSchedule()) {
            return "redirect:/submitted-schedules";
        }

        if (schedulePlan.getAllCourses().isEmpty()) {
            model.addAttribute("submitError", "Select at least one course before submitting.");
            model.addAttribute("graduationAlert", schedulePlan.getGraduationAlert());
            model.addAttribute("isViewOnlyRole", !schedulePlan.canModifySchedule());
            return "schedule";
        }

        ScheduleSubmission submission = new ScheduleSubmission();
        submission.setUsername(schedulePlan.getUsername());
        submission.setMajorName(
                schedulePlan.getSelectedMajor() != null ? schedulePlan.getSelectedMajor().getName() : ""
        );
        submission.setTotalCredits(schedulePlan.getTotalCredits());
        submission.setExpectedGraduationDate(schedulePlan.getExpectedGraduationDate());

        //Save completed courses
        submission.setCompletedCourseIdsCsv(schedulePlan.getCompletedCourseIdsCsv());

        for (int i = 0; i < schedulePlan.getSemesters().size(); i++) {
            SemesterPlan semesterPlan = schedulePlan.getSemesters().get(i);

            SubmittedSemester submittedSemester = new SubmittedSemester();
            submittedSemester.setName(semesterPlan.getName());
            submittedSemester.setSemesterIndex(i);
            submittedSemester.setSubmission(submission);
            submittedSemester.getCourses().addAll(semesterPlan.getCourses());

            //Save add/waitlist statuses
            submittedSemester.setCourseStatusesCsv(schedulePlan.buildCourseStatusesCsvForSemester(semesterPlan));

            submission.getSemesters().add(submittedSemester);
        }

        submissionRepo.save(submission);
        status.setComplete();
        return "redirect:/confirmation";
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "confirmation";
    }

    //Shared helpers for live page updates and filtered electives
    private void populateCoursesModel(SchedulePlan schedulePlan, Model model) {
        model.addAttribute("courses", getVisibleCourses(schedulePlan));
        model.addAttribute("graduationAlert", schedulePlan.getGraduationAlert());
    }

    private List<Course> getVisibleCourses(SchedulePlan schedulePlan) {
        List<Course> visibleCourses = new ArrayList<>();

        courseRepo.findAll().forEach(course -> {
            if (schedulePlan.isCourseRelevantForSelectedMajor(course)) {
                visibleCourses.add(course);
            }
        });

        visibleCourses.sort(Comparator.comparing(Course::getCode));
        return visibleCourses;
    }
}