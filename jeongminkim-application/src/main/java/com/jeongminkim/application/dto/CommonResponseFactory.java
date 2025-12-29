package com.jeongminkim.application.dto;

import com.jeongminkim.application.common.time.TimeProvider;
import com.jeongminkim.application.dto.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonResponseFactory {

    private final TimeProvider timeProvider;

    /**
     * 성공 응답 생성 (데이터 있음)
     */
    public <T> CommonResponse<T> success(T data, ResponseMessage responseMessage) {
        return CommonResponse.success(data, responseMessage, timeProvider.now());
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public <T> CommonResponse<T> success(ResponseMessage responseMessage) {
        return CommonResponse.success(responseMessage, timeProvider.now());
    }

    /**
     * 실패 응답 생성
     */
    public <T> CommonResponse<T> error(String code, String message) {
        return CommonResponse.error(code, message, timeProvider.now());
    }
}