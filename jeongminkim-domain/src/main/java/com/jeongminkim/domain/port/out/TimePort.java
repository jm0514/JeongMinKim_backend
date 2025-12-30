package com.jeongminkim.domain.port.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 시간 제공 포트 (Outbound Port)
 * Infrastructure 계층에서 구현
 * 테스트 시 시간 제어를 위한 인터페이스
 */
public interface TimePort {

    /**
     * 현재 시간 조회
     */
    LocalDateTime now();

    /**
     * 현재 날짜 조회
     */
    LocalDate today();
}