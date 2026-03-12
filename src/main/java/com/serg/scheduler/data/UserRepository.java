package com.serg.scheduler.data;

import com.serg.scheduler.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

//Spring data repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username); //Spring reads method name and builds db query
}