package com.jeongminkim_backend.common.time;

import java.time.LocalDateTime;

public interface TimeProvider {

    LocalDateTime now();
}
