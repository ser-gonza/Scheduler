package com.serg.scheduler.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Major {

    @Id
    private Long id;

    private String name;

    private int totalCreditsRequired;
    private int artsHumanitiesRequired;
    private int mathScienceRequired;
    private int majorCreditsRequired;
}
