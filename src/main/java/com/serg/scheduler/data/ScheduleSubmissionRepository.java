package com.serg.scheduler.data;

import com.serg.scheduler.domain.ScheduleSubmission;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ScheduleSubmissionRepository extends CrudRepository<ScheduleSubmission, Long> {
    //Finds submissions, sort by createdAt in descending order, return the newest one
    Optional<ScheduleSubmission> findTopByUsernameOrderByCreatedAtDesc(String username);
}