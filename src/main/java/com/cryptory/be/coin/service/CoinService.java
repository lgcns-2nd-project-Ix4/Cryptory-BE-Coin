package com.cryptory.be.coin.service;

import java.util.List;

import com.cryptory.be.coin.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface CoinService {
	List<CoinDto> getCoins();
	CoinDetailDto getCoinDetail(Long coinId);
	List<CoinNewsDto> getCoinNews(Long coinId);
	void updateDisplaySetting(Long coinId, boolean isDisplayed);
	Page<CoinListResponseDto> getCoinListForAdmin(String keyword, int page, int size, String sort);
	CoinDetailResponseDto getCoinDetailsForAdmin(Long coinId);
}
