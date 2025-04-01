package com.cryptory.be.coin.controller;

import java.util.List;
import java.util.NoSuchElementException;

import com.cryptory.be.coin.dto.CoinDetailDto;
import com.cryptory.be.coin.dto.CoinNewsDto;
import com.cryptory.be.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cryptory.be.coin.dto.CoinDto;
import com.cryptory.be.coin.service.CoinService;

@Slf4j
@RestController
@RequestMapping("/api/v1/coins")
@RequiredArgsConstructor
public class CoinController {

	private final CoinService coinService;
	
	// 코인 목록 조회
	@GetMapping
	public ApiResponse<CoinDto> getCoins() throws Exception {
		
		List<CoinDto> coinList = coinService.getCoins();

		return new ApiResponse<>(HttpStatus.OK, coinList);
	}
	
	// 특정 코인 상세 조회
	@GetMapping("/{coinId}")
	public ApiResponse<CoinDetailDto> getCoinDetail(@PathVariable("coinId") Long coinId) {
		
		CoinDetailDto selectedCoinDetail = coinService.getCoinDetail(coinId);

		return new ApiResponse<>(HttpStatus.OK, selectedCoinDetail);
		
	}
	
	// 특정 코인 뉴스 조회 - 네이버 뉴스
	@GetMapping("/{coinId}/news")
	public ApiResponse<CoinNewsDto> searchCoinNews(@PathVariable("coinId") Long coinId) {
		List<CoinNewsDto> coinNewsList = coinService.getCoinNews(coinId);
		return new ApiResponse<>(HttpStatus.OK, coinNewsList);
	}

	// ---> 코인 메인 페이지 노출 여부 변경 (내부 API용)
	@PatchMapping("/{coinId}/display")
	public ResponseEntity<?> updateDisplaySetting(
												   @PathVariable Long coinId,
												   @RequestParam("isDisplayed") boolean isDisplayed) {
		try {
			coinService.updateDisplaySetting(coinId, isDisplayed);
			log.info("(CoinController) Request processed to update display setting for coinId: {}, isDisplayed: {}", coinId, isDisplayed);
			return ResponseEntity.ok().build(); // 성공
		} catch (NoSuchElementException e) {
			log.warn("(CoinController) Coin not found for ID {} during display update.", coinId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (IllegalArgumentException e) {
			log.warn("(CoinController) Business rule violation for coinId {}: {}", coinId, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			log.error("(CoinController) Internal server error updating display setting for coinId {}: {}", coinId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류 발생");
		}
	}

}
