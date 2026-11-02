package com.cfs.credit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CreditAssessmentServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                CreditAssessmentServiceApplication.class,
                args
        );
    }
}