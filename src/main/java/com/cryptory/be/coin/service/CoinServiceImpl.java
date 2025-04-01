package com.cryptory.be.coin.service;

import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import com.cryptory.be.chart.domain.Chart;
import com.cryptory.be.chart.dto.ChartDto;
import com.cryptory.be.chart.exception.ChartErrorCode;
import com.cryptory.be.chart.exception.ChartException;
import com.cryptory.be.chart.repository.ChartRepository;
import com.cryptory.be.coin.domain.Coin;
import com.cryptory.be.coin.dto.*;
import com.cryptory.be.coin.exception.CoinErrorCode;
import com.cryptory.be.coin.exception.CoinException;
import com.cryptory.be.global.util.DateFormat;
import com.cryptory.be.issue.dto.IssueDto;
import com.cryptory.be.issue.repository.IssueRepository;
import com.cryptory.be.openapi.dto.NaverNews;
import com.cryptory.be.openapi.dto.Ticker;
import com.cryptory.be.openapi.service.NaverService;
import com.cryptory.be.openapi.service.UpbitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import com.cryptory.be.coin.repository.CoinRepository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoinServiceImpl implements CoinService {

    private final NaverService naverService;
    private final UpbitService upbitService;

    private final CoinRepository coinRepository;
    private final ChartRepository chartRepository;
    private final IssueRepository issueRepository;

    private final int END_OF_KRW = 4;
    private static final int MAX_DISPLAYED_COINS = 7; // 노출 제한 개수 상수

    // 관리자용 Openfeign 메서드
    // 코인 목록 조회
    public Page<CoinListResponseDto> getCoinListForAdmin(String keyword, int page, int size, String sort){
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page,size, sorting);

        String parsedKeyword = null;
        if(keyword != null && !keyword.trim().isEmpty()){
            parsedKeyword = "%" + keyword.toLowerCase().trim() + "%";
        }
        log.debug("관리자 코인 목록 검색 키워드: {}", parsedKeyword);

        Page<Coin> coinsPage;
        if (parsedKeyword == null) {
            // 관리자는 isDisplayed=false인 코인도 볼 수 있어야 할 수 있음 - 요구사항에 따라 findAll 또는 다른 쿼리 사용
            coinsPage = coinRepository.findAll(pageable);
        } else {
            // 관리자 검색 로직 - 요구사항에 따라 searchCoins 또는 다른 쿼리 사용
            coinsPage = coinRepository.searchCoins(parsedKeyword, pageable);
        }

        // Page<Coin> -> Page<CoinListResponseDto> 변환
        return coinsPage.map(this::convertToCoinListResponseDto);
    }

    // 특정코인 조회 - 관리자용
    public CoinDetailResponseDto getCoinDetailsForAdmin(Long coinId){
        Coin coin = coinRepository.findById(coinId)
                .orElseThrow(() -> new NoSuchElementException("해당 코인을 찾을 수 없습니다ㅏ. ID: " + coinId));
        // Coin -> CoinDetailResponseDto 변환
        return convertToCoinDetailResponseDto(coin);
    }

    @Override
    @Transactional
    public void updateDisplaySetting(Long coinId, boolean isDisplayed){
        Coin coin = coinRepository.findById(coinId)
                .orElseThrow(() -> new NoSuchElementException("해당 코인을 찾을 수 없습니다. ID: " + coinId));

        // isDisplayed를 true로 설정하려는 경우 + 현재 노출 상태가 아닌 경우에만 개수 제한 확인
        if (isDisplayed && !coin.isDisplayed()) {
            long currentDisplayedCount = coinRepository.countByIsDisplayedTrue();
            if (currentDisplayedCount >= MAX_DISPLAYED_COINS) {
                throw new IllegalArgumentException("메인 페이지에 노출 가능한 코인 수(" + MAX_DISPLAYED_COINS + "개)를 초과했습니다.");
            }
        }
        // 상태 변경
        coin.setIsDisplayed(isDisplayed);
        // @Transactional에 의해 변경 감지로 저장됨
        log.info("(CoinService) Coin display status updated for ID: {}, isDisplayed: {}", coinId, isDisplayed);
    }

    // 코인 목록 조회
    @Override
    public List<CoinDto> getCoins() {

        // 화면에 보여지는 코인만 조회
        List<Coin> coins = coinRepository.findAll().stream()
                .filter(Coin::isDisplayed)
                .toList();

        if (coins.isEmpty()) {
            throw new CoinException(CoinErrorCode.COIN_DATA_MISSING);
        }

        // 코인 코드 목록
        String[] codes = coins.stream()
                .map(Coin::getCode)
                .toArray(String[]::new);

        // 코인 목록에선 현재가, 변화액, 변화율 필요
        List<Ticker> tickers = upbitService.getTickers(codes);

        // Dto 담기 위해 Map으로 변환
        Map<String, Ticker> tickerMap = tickers.stream()
                .collect(Collectors.toMap(Ticker::getMarket, ticker -> ticker));

        // log.info("tickerMap: {}", tickerMap);

        return coins.stream()
                .map(coin ->
                        CoinDto.builder()
                                .coinId(coin.getId())
                                .koreanName(coin.getKoreanName())
                                .englishName(coin.getEnglishName())
                                .code(coin.getCode().substring(END_OF_KRW)) // "KRW-" 제거
                                .coinSymbol(coin.getCoinSymbol())
                                .tradePrice(tickerMap.get(coin.getCode()).getTradePrice())
                                .signedChangePrice(tickerMap.get(coin.getCode()).getSignedChangePrice())
                                .signedChangeRate(tickerMap.get(coin.getCode()).getSignedChangeRate())
                                .build())
                .toList();
    }

    // 특정 코인 상세 조회
    @Override
    public CoinDetailDto getCoinDetail(Long coinId) {
        Coin coin = coinRepository.findById(coinId)
                .orElseThrow(() -> new CoinException(CoinErrorCode.COIN_DATA_MISSING));

        // 코인의 차트 데이터 조회
        List<ChartDto> charts = chartRepository.findAllByCoinId(coin.getId()).stream()
                .map(chart -> ChartDto.builder()
                        .chartId(chart.getId())
                        .date(chart.getDate())
                        .openingPrice(chart.getOpeningPrice())
                        .tradePrice(chart.getTradePrice())
                        .highPrice(chart.getHighPrice())
                        .lowPrice(chart.getLowPrice())
                        .changeRate(chart.getChangeRate())
                        .build())
                .toList();

        if (charts.isEmpty()) {
            throw new ChartException(ChartErrorCode.CHART_DATA_MISSING);
        }

        // 코인 하나의 현재가(Ticker) 반환
        Ticker coinTicker = upbitService.getTickers(coin.getCode()).get(0);

        // 이슈 목록 조회
        List<IssueDto> issues = issueRepository.findAllByCoinId(coin.getId()).stream()
                .map(issue -> IssueDto.builder()
                        .issueId(issue.getId())
                        .chartId(issue.getChart().getId())
                        .date(issue.getChart().getDate())
                        .openingPrice(issue.getChart().getOpeningPrice())
                        .highPrice(issue.getChart().getHighPrice())
                        .lowPrice(issue.getChart().getLowPrice())
                        .tradePrice(issue.getChart().getTradePrice())
                        .build())
                .toList();

        return CoinDetailDto.builder()
                .coinId(coin.getId())
                .koreanName(coin.getKoreanName())
                .englishName(coin.getEnglishName())
                .code(coin.getCode().substring(END_OF_KRW)) // KRW- 제거
                .coinSymbol(coin.getCoinSymbol())
                .tradePrice(coinTicker.getTradePrice())
                .signedChangeRate(coinTicker.getSignedChangeRate())
                .signedChangePrice(coinTicker.getSignedChangePrice())
                .timestamp(DateFormat.formatTradeTime(coinTicker.getTradeDate(), coinTicker.getTradeTime()))
                .chartList(charts)
                .issueList(issues)
                .build();

    }

    // 특정 코인 뉴스 조회
    @Override
    public List<CoinNewsDto> getCoinNews(Long coinId) {
        Coin coin = coinRepository.findById(coinId)
                .orElseThrow(() -> new CoinException(CoinErrorCode.COIN_DATA_MISSING));

        List<NaverNews> naverNewsList = naverService.getNaverNewsWithWord(coin.getKoreanName());

        return naverNewsList.stream()
                .map(naverNews -> {
                    try {
                        return new CoinNewsDto(naverNews.getTitle(), naverNews.getLink(),
                                naverNews.getDescription(), DateFormat.formatNewsDate(naverNews.getPubDate()));
                    } catch (ParseException e) {
                        throw new CoinException(CoinErrorCode.COIN_NEWS_PARSE_ERROR);
                    }
                })
                .toList();
    }


    // 헬퍼함수들 적용
    private CoinListResponseDto convertToCoinListResponseDto(Coin coin) {
        // coin-service 내부에 정의된 com.cryptory.be.coin.dto.CoinListResponseDto 사용 가정
        String logoUrl = (coin.getCoinSymbol() != null) ? coin.getCoinSymbol().getLogoUrl() : null;
        // Coin.code ("KRW-BTC") 에서 "KRW-" 제거 후 symbol로 사용
        String symbolCode = (coin.getCode() != null && coin.getCode().startsWith("KRW-"))
                ? coin.getCode().substring(END_OF_KRW)
                : coin.getCode();

        return CoinListResponseDto.builder()
                .cryptoId(coin.getId())
                .koreanName(coin.getKoreanName())
                .englishName(coin.getEnglishName())
                .symbol(symbolCode) // 예: BTC
                .logoUrl(logoUrl)
                .isDisplayed(coin.isDisplayed())
                .build();
    }

    private CoinDetailResponseDto convertToCoinDetailResponseDto(Coin coin) {
        String logoUrl = (coin.getCoinSymbol() != null) ? coin.getCoinSymbol().getLogoUrl() : null;
        String cryptoColor = (coin.getCoinSymbol() != null) ? coin.getCoinSymbol().getColor() : null;
        String symbolCode = (coin.getCode() != null && coin.getCode().startsWith("KRW-"))
                ? coin.getCode().substring(END_OF_KRW)
                : coin.getCode();

        return CoinDetailResponseDto.builder()
                .cryptoId(coin.getId())
                .name(coin.getKoreanName()) // DTO 필드명 'name' 사용
                .symbol(symbolCode) // 예: BTC
                .logoUrl(logoUrl)
                .cryptoColor(cryptoColor)
                .isDisplayed(coin.isDisplayed())
                .build();
    }

    private Sort parseSort(String sort) {
        Sort defaultSort = Sort.by("id").ascending();
        if (sort == null || sort.trim().isEmpty()) {
            return defaultSort;
        }
        try {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            if (property.isEmpty()) return defaultSort;

            Sort.Direction direction = Sort.Direction.ASC;
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
                direction = Sort.Direction.DESC;
            }
            // TODO: property가 Coin 엔티티에 실제 존재하는 필드인지 검증하는 로직 추가 고려
            return Sort.by(direction, property);
        } catch (Exception e) {
            log.warn("정렬 파라미터 파싱 오류: '{}'. 기본 정렬 적용.", sort, e);
            return defaultSort;
        }
    }

}
