package com.serg.scheduler.data;

import com.serg.scheduler.domain.Major;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface MajorRepository extends CrudRepository<Major, Long> {
    Optional<Major> findByNameIgnoreCase(String name);
}
