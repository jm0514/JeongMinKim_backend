package com.jeongminkim.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.jeongminkim.domain",
    "com.jeongminkim.application",
    "com.jeongminkim.infrastructure",
    "com.jeongminkim.api"
})
public class JeongminkimBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JeongminkimBackendApplication.class, args);
    }

}
