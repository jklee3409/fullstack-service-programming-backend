package com.mycom.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodeScanApplication {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("!!! CHECKING RENDER ENVIRONMENT VARIABLES !!!");
        System.out.println("DATASOURCE URL: " + System.getenv("SPRING_DATASOURCE_URL"));
        System.out.println("DATASOURCE USERNAME: " + System.getenv("SPRING_DATASOURCE_USERNAME"));

        String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        System.out.println("DATASOURCE PASSWORD is present: " + (password != null && !password.isEmpty()));
        System.out.println("============================================================");

        SpringApplication.run(CodeScanApplication.class, args);
    }

}
