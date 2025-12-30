package com.jeongminkim.application.dto;

import com.jeongminkim.application.dto.response.ResponseMessage;
import com.jeongminkim.domain.port.out.TimePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonResponseFactory {

    private final TimePort timePort;

    /**
     * 성공 응답 생성 (데이터 있음)
     */
    public <T> CommonResponse<T> success(T data, ResponseMessage responseMessage) {
        return CommonResponse.success(data, responseMessage, timePort.now());
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public <T> CommonResponse<T> success(ResponseMessage responseMessage) {
        return CommonResponse.success(responseMessage, timePort.now());
    }

    /**
     * 실패 응답 생성
     */
    public <T> CommonResponse<T> error(String code, String message) {
        return CommonResponse.error(code, message, timePort.now());
    }
}
