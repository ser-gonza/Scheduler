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
    public String majors(Model model) {
        model.addAttribute("majors", majorRepo.findAll());
        return "majors";
    }

    //Gets chosen major ID and finds major in database, stores in current session's schedulePlan, redirects to courses page
    @PostMapping("/majors/select")
    public String selectMajor(@RequestParam("majorId") Long majorId,
                              @ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
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
        if (schedulePlan.getSelectedMajor() == null) {
            return "redirect:/majors";
        }

        model.addAttribute("courses", courseRepo.findAll());
        return "courses";
    }

    //Shows 8 semester schedule
    @GetMapping("/schedule")
    public String schedule(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        //Checks login because page is displaying current session plan
        if (schedulePlan.getUsername() == null || schedulePlan.getUsername().isBlank()) {
            return "redirect:/login";
        }
        return "schedule";
    }

    //Gets selected course ID, loads course from database, checks if prereqs are satisfied, if not shows error, if yes add course to current semester
    @PostMapping("/schedule/add")
    public String add(@RequestParam("courseId") Long courseId,
                      @ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                      Model model) {
        Course course = courseRepo.findById(courseId).orElse(null);

        if (course != null) {
            if (!schedulePlan.canTakeCourse(course)) {
                model.addAttribute("courses", courseRepo.findAll());
                model.addAttribute("addError",
                        "Cannot add " + course.getCode() + ". Missing prerequisite(s): "
                                + schedulePlan.getMissingPrerequisites(course));
                return "courses";
            }

            schedulePlan.addCourse(course);
        }

        return "redirect:/courses";
    }

    //Removes course
    @PostMapping("/schedule/remove")
    public String remove(@RequestParam("courseId") Long courseId,
                         @ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        schedulePlan.removeCourse(courseId);
        return "redirect:/courses";
    }

    //Resets plan to 8 empty semesters
    @PostMapping("/schedule/reset")
    public String reset(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        schedulePlan.reset();
        return "redirect:/courses";
    }

    //Move active semester forward/backward
    @PostMapping("/schedule/next-semester")
    public String nextSemester(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        schedulePlan.nextSemester();
        return "redirect:/courses";
    }

    //Move active semester forward/backward
    @PostMapping("/schedule/previous-semester")
    public String previousSemester(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        schedulePlan.previousSemester();
        return "redirect:/courses";
    }

    //Submits the plan, makes sure it is not empty,
    @PostMapping("/schedule/submit")
    public String submit(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                         SessionStatus status,
                         Model model) {

        if (schedulePlan.getAllCourses().isEmpty()) {
            model.addAttribute("submitError", "Select at least one course before submitting.");
            return "schedule";
        }

        ScheduleSubmission submission = new ScheduleSubmission();
        submission.setUsername(schedulePlan.getUsername());
        submission.setMajorName(
                schedulePlan.getSelectedMajor() != null ? schedulePlan.getSelectedMajor().getName() : ""
        );
        submission.setTotalCredits(schedulePlan.getTotalCredits());
        submission.setExpectedGraduationDate(schedulePlan.getExpectedGraduationDate());

        for (int i = 0; i < schedulePlan.getSemesters().size(); i++) {
            SemesterPlan semesterPlan = schedulePlan.getSemesters().get(i);

            SubmittedSemester submittedSemester = new SubmittedSemester();
            submittedSemester.setName(semesterPlan.getName());
            submittedSemester.setSemesterIndex(i);
            submittedSemester.setSubmission(submission);
            submittedSemester.getCourses().addAll(semesterPlan.getCourses());

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
}