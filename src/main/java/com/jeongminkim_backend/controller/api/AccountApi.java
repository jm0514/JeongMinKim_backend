package com.jeongminkim_backend.controller.api;

import com.jeongminkim_backend.dto.request.CreateAccountRequest;
import com.jeongminkim_backend.dto.response.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "계좌 관리 API", description = "계좌 등록, 조회, 삭제 기능을 제공합니다.")
public interface AccountApi {

    @Operation(summary = "계좌 생성", description = "새로운 계좌를 등록합니다. 계좌번호는 10~20자리여야 하며, 중복될 수 없습니다.")
    @ApiResponse(responseCode = "201", description = "계좌 생성 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class)))
    @CommonApiResponses.BadRequest
    @CommonApiResponses.Conflict
    ResponseEntity<com.jeongminkim_backend.dto.CommonResponse<AccountResponse>> createAccount(
            CreateAccountRequest request
    );

    @Operation(summary = "계좌 조회", description = "계좌번호로 계좌 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "계좌 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class)))
    @CommonApiResponses.NotFound
    ResponseEntity<com.jeongminkim_backend.dto.CommonResponse<AccountResponse>> getAccount(
            @Parameter(description = "조회할 계좌번호", example = "1234567890", required = true)
            String accountNumber
    );

    @Operation(summary = "계좌 삭제", description = "계좌번호로 계좌를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "계좌 삭제 성공",
            content = @Content(mediaType = "application/json"))
    @CommonApiResponses.NotFound
    ResponseEntity<com.jeongminkim_backend.dto.CommonResponse<Void>> deleteAccount(
            @Parameter(description = "삭제할 계좌번호", example = "1234567890", required = true)
            String accountNumber
    );
}