package com.jeongminkim.api.controller.api;

import com.jeongminkim.application.dto.CommonResponse;
import com.jeongminkim.application.dto.request.DepositRequest;
import com.jeongminkim.application.dto.request.TransferRequest;
import com.jeongminkim.application.dto.request.WithdrawRequest;
import com.jeongminkim.application.dto.response.TransactionResponse;
import com.jeongminkim.application.dto.response.TransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "거래 API", description = "입금, 출금, 이체 및 거래 내역 조회 기능을 제공합니다.")
public interface TransactionApi {

    @Operation(summary = "입금", description = "특정 계좌에 금액을 입금합니다.")
    @ApiResponse(responseCode = "200", description = "입금 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class)))
    @CommonApiResponses.BadRequest
    @CommonApiResponses.NotFound
    ResponseEntity<CommonResponse<TransactionResponse>> deposit(
            DepositRequest request
    );

    @Operation(summary = "출금", description = "특정 계좌에서 금액을 출금합니다. 잔액이 충분해야 하며, 일일 출금 한도는 1,000,000원입니다.")
    @ApiResponse(responseCode = "200", description = "출금 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class)))
    @CommonApiResponses.BadRequest
    @CommonApiResponses.NotFound
    ResponseEntity<CommonResponse<TransactionResponse>> withdraw(
            WithdrawRequest request
    );

    @Operation(summary = "이체", description = "출금 계좌에서 입금 계좌로 금액을 이체합니다. 이체 금액의 1%가 수수료로 부과되며, 일일 이체 한도는 3,000,000원입니다.")
    @ApiResponse(responseCode = "200", description = "이체 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponse.class)))
    @CommonApiResponses.BadRequest
    @CommonApiResponses.NotFound
    ResponseEntity<CommonResponse<TransferResponse>> transfer(
            TransferRequest request
    );

    @Operation(summary = "거래 내역 조회", description = "지정된 계좌의 거래 내역을 최신순으로 조회합니다. 페이징을 지원합니다.")
    @ApiResponse(responseCode = "200", description = "거래 내역 조회 성공")
    @CommonApiResponses.NotFound
    ResponseEntity<CommonResponse<Page<TransactionResponse>>> getTransactions(
            @Parameter(description = "조회할 계좌번호", example = "1234567890", required = true)
            String accountNumber,
            @Parameter(description = "페이지 정보 (page, size, sort)", example = "page=0&size=20&sort=createdAt,desc")
            Pageable pageable
    );
}