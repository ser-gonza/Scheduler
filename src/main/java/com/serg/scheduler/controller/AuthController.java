package com.serg.scheduler.controller;

import com.serg.scheduler.data.MajorRepository;
import com.serg.scheduler.data.ScheduleSubmissionRepository;
import com.serg.scheduler.data.UserRepository;
import com.serg.scheduler.domain.SchedulePlan;
import com.serg.scheduler.domain.ScheduleSubmission;
import com.serg.scheduler.domain.SemesterPlan;
import com.serg.scheduler.domain.SubmittedSemester;
import com.serg.scheduler.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

@Controller
@SessionAttributes("schedulePlan")
public class AuthController {

    private final ScheduleSubmissionRepository submissionRepo;
    private final MajorRepository majorRepo;
    private final UserRepository userRepo;

    public AuthController(ScheduleSubmissionRepository submissionRepo,
                          MajorRepository majorRepo,
                          UserRepository userRepo) {
        this.submissionRepo = submissionRepo;
        this.majorRepo = majorRepo;
        this.userRepo = userRepo;
    }

    //Starting plan object for user, creates session object
    @ModelAttribute("schedulePlan")
    public SchedulePlan schedulePlan() {
        return new SchedulePlan();
    }

    //Root route
    @GetMapping("/")
    public String root(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        if (schedulePlan.getUsername() == null || schedulePlan.getUsername().isBlank()) {
            return "redirect:/login";
        }
        if (schedulePlan.getSelectedMajor() == null) {
            return "redirect:/majors";
        }
        return "redirect:/courses";
    }

    //Just shows login
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                        Model model) {

        Optional<User> existingUser = userRepo.findByUsername(username);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (!user.getPassword().equals(password)) {
                model.addAttribute("loginError", "Incorrect password.");
                return "login";
            }
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            userRepo.save(newUser);
        }

        Optional<ScheduleSubmission> savedSubmission =
                submissionRepo.findTopByUsernameOrderByCreatedAtDesc(username);

        if (savedSubmission.isPresent()) {
            ScheduleSubmission submission = savedSubmission.get();

            schedulePlan.setUsername(username);
            schedulePlan.setExpectedGraduationDate(submission.getExpectedGraduationDate());

            if (submission.getMajorName() != null && !submission.getMajorName().isBlank()) {
                majorRepo.findByNameIgnoreCase(submission.getMajorName())
                        .ifPresent(schedulePlan::setSelectedMajor);
            }

            schedulePlan.setSemesters(new ArrayList<>());

            submission.getSemesters().stream()
                    .sorted(Comparator.comparingInt(SubmittedSemester::getSemesterIndex))
                    .forEach(savedSemester -> {
                        SemesterPlan semesterPlan = new SemesterPlan(savedSemester.getName());
                        semesterPlan.getCourses().addAll(savedSemester.getCourses());
                        schedulePlan.getSemesters().add(semesterPlan);
                    });

            while (schedulePlan.getSemesters().size() < 8) {
                schedulePlan.getSemesters().add(
                        new SemesterPlan("Semester " + (schedulePlan.getSemesters().size() + 1))
                );
            }

            schedulePlan.setCurrentSemesterIndex(0);

            if (schedulePlan.getSelectedMajor() != null) {
                return "redirect:/courses";
            }

            return "redirect:/majors";
        }

        schedulePlan.reset();
        schedulePlan.setUsername(username);
        schedulePlan.setExpectedGraduationDate(calculateExpectedGraduationDate());

        return "redirect:/majors";
    }

    @PostMapping("/logout")
    public String logout(SessionStatus status) {
        status.setComplete();
        return "redirect:/login";
    }

    private String calculateExpectedGraduationDate() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        if (month <= 5) {
            return "Spring " + (year + 4);
        } else {
            return "Fall " + (year + 4);
        }
    }
}