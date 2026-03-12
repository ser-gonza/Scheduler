package com.serg.scheduler.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_user") //Represents row in db table
@Data
//Stores users and passwords
public class User {

    //db primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Each username must be unique and not null
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
}