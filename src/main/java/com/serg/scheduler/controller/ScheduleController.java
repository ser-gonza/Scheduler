package com.serg.scheduler.controller;

import com.serg.scheduler.data.ScheduleSubmissionRepository;
import com.serg.scheduler.domain.SchedulePlan;
import com.serg.scheduler.domain.ScheduleSubmission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

//Show schedule review page and saves when submitted (schedule review + persistence save)

@Controller
@RequiredArgsConstructor
@SessionAttributes("schedulePlan") //Allows to read the same plan from the session
public class ScheduleController {

    private final ScheduleSubmissionRepository submissionRepo;

    @ModelAttribute("schedulePlan") //Avoids missing attributes
    public SchedulePlan schedulePlan() {
        return new SchedulePlan();
    }

    @ModelAttribute("scheduleSubmission") //Creates entity object that gets saved when submitted
    public ScheduleSubmission scheduleSubmission() {
        return new ScheduleSubmission();
    }

    @GetMapping("/schedule") //Gets schedule then renders templates/schedule.html
    public String schedule() {
        return "schedule";
    }

    @PostMapping("/schedule/submit") //Copies selected courses to ScheduleSubmission
    public String submit(@ModelAttribute("scheduleSubmission") ScheduleSubmission submission,
                         Errors errors,
                         @ModelAttribute("schedulePlan") SchedulePlan schedulePlan,
                         SessionStatus status) {

        if (schedulePlan.getCourses().isEmpty()) { //Checks if schedulePlan has courses
            errors.reject("coursesEmpty", "Select at least one course before submitting.");
            return "schedule";
        }

        submission.getCourses().clear();
        submission.getCourses().addAll(schedulePlan.getCourses());

        submissionRepo.save(submission); //Saves submission to repo
        status.setComplete(); //Clears session
        return "redirect:/confirmation"; //Redirects to confirmation message
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "confirmation";
    }
}