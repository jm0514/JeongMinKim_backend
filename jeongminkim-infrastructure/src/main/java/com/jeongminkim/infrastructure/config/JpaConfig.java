package com.jeongminkim.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.jeongminkim.infrastructure.persistence.jpa.repository")
@EntityScan(basePackages = "com.jeongminkim.infrastructure.persistence.jpa.entity")
public class JpaConfig {
}