package com.jeongminkim.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.jeongminkim.core",
    "com.jeongminkim.application",
    "com.jeongminkim.api"
})
@EntityScan("com.jeongminkim.core.domain.entity")
@EnableJpaRepositories("com.jeongminkim.core.repository")
public class JeongminkimBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JeongminkimBackendApplication.class, args);
    }

}
