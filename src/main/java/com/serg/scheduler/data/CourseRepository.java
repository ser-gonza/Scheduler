package com.serg.scheduler.data;

import com.serg.scheduler.domain.Course;
import org.springframework.data.repository.CrudRepository;

//Gives database access methods for courses e.g findAll(), add/update()
public interface CourseRepository extends CrudRepository<Course, Long> {} //Course entitys (id, name, credits)
