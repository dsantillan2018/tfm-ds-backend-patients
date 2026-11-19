package com.dsantillan.tfm.patients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.dsantillan.tfm.patients",
                "com.dsantillan.tfm.common",
        }
)
public class PatientsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientsApplication.class, args);
    }

}
