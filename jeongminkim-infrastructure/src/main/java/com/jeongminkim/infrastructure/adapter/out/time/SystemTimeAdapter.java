package com.jeongminkim.infrastructure.adapter.out.time;

import com.jeongminkim.domain.port.out.TimePort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * System Time Adapter
 * TimePort 구현체 (Outbound Adapter)
 */
@Component
public class SystemTimeAdapter implements TimePort {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}