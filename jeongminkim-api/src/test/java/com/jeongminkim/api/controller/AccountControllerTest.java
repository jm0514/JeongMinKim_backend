package com.jeongminkim.api.controller;

import com.jeongminkim.application.dto.request.CreateAccountRequest;
import com.jeongminkim.api.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AccountController 통합 테스트")
class AccountControllerTest extends IntegrationTestSupport {

    @Test
    @DisplayName("계좌 생성 성공 - 201 Created")
    void createAccount_success() throws Exception {
        // given
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountNumber(TEST_ACCOUNT_1)
                .ownerName(TEST_OWNER_1)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(TEST_ACCOUNT_1))
                .andExpect(jsonPath("$.data.ownerName").value(TEST_OWNER_1))
                .andExpect(jsonPath("$.data.balance").value(0));
    }

    @Test
    @DisplayName("계좌 조회 성공 - 200 OK")
    void getAccount_success() throws Exception {
        // given - 먼저 계좌 생성
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);

        // when & then
        mockMvc.perform(get("/api/v1/accounts/" + TEST_ACCOUNT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(TEST_ACCOUNT_1))
                .andExpect(jsonPath("$.data.ownerName").value(TEST_OWNER_1));
    }

    @Test
    @DisplayName("계좌 삭제 성공 - 204 No Content")
    void deleteAccount_success() throws Exception {
        // given - 먼저 계좌 생성
        createTestAccount(TEST_ACCOUNT_1, TEST_OWNER_1);

        // when & then
        mockMvc.perform(delete("/api/v1/accounts/" + TEST_ACCOUNT_1))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("계좌 생성 실패 - Validation (계좌번호 형식) - 400 Bad Request")
    void createAccount_fail_validation() throws Exception {
        // given
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountNumber("123")  // 10자리 미만
                .ownerName(TEST_OWNER_1)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
