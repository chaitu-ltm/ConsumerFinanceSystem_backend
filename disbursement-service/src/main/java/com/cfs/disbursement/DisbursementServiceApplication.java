package com.cfs.disbursement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DisbursementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisbursementServiceApplication.class, args);
    }

}
