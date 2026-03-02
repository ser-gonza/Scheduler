package com.serg.scheduler.controller;

import com.serg.scheduler.data.CourseRepository;
import com.serg.scheduler.domain.Course;
import com.serg.scheduler.domain.SchedulePlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//Handles course browsing and add/remove/reset buttons

@Controller
@RequiredArgsConstructor
@SessionAttributes("schedulePlan")
public class CourseController {

    private final CourseRepository courseRepo;

    //Keeps a model attribute in HTTP session (schedulePlan)
    @ModelAttribute("schedulePlan") //Puts object into the model so it can be used in the view
    public SchedulePlan schedulePlan() {
        return new SchedulePlan();
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        model.addAttribute("courses", courseRepo.findAll()); //Pulls all courses from DB, puts into model named courses
        return "courses"; //Returns view
    }
    //Thymeleaf renders templates/courses.html

    //Receives courseId
    @PostMapping("/schedule/add")
    //Loads course from DB
    public String add(@RequestParam("courseId") Long courseId, //Grabs the request
                      @ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {

        Course course = courseRepo.findById(courseId).orElse(null);
        if (course != null) {
            schedulePlan.addCourse(course); //Adds to session plan
        }
        return "redirect:/schedule"; //Redirects to schedule
    }

    //Receives courseId
    @PostMapping("/schedule/remove")
    //Loads course from DB
    public String remove(@RequestParam("courseId") Long courseId,
                         @ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        schedulePlan.removeCourse(courseId); //Removes from session plan
        return "redirect:/schedule"; //Redirects to schedule
    }

    @PostMapping("/schedule/reset")
    public String reset(@ModelAttribute("schedulePlan") SchedulePlan schedulePlan) {
        schedulePlan.reset(); //Clears session plan
        return "redirect:/schedule"; //Redirects to schedule
    }
}
