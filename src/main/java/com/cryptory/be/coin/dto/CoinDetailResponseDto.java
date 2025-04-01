package com.cryptory.be.coin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * packageName    : com.cryptory.be.coin.dto
 * fileName       : CoinDetailResponseDto
 * author         : 조영상
 * date           : 4/1/25
 * description    : 자동 주석 생성
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 4/1/25         조영상        최초 생성
 */
@Getter
@Builder
@AllArgsConstructor
@Jacksonized
public class CoinDetailResponseDto {
    private Long cryptoId;
    private String name;
    private String symbol;
    private String logoUrl;
    private boolean isDisplayed;
    private String cryptoColor;
}
