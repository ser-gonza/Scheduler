package com.serg.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//Starts spring boot application
@SpringBootApplication
public class ScheduleApplication {

    //Runs app and starts tomcat
	public static void main(String[] args) {
		SpringApplication.run(ScheduleApplication.class, args);
	}

}
