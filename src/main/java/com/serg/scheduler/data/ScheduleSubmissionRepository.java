package com.serg.scheduler.data;

import com.serg.scheduler.domain.ScheduleSubmission;
import org.springframework.data.repository.CrudRepository;

//Used for save submission, DB access for saved entries
public interface ScheduleSubmissionRepository extends CrudRepository<ScheduleSubmission, Long> {}
