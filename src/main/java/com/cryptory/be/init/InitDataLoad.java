package com.cryptory.be.init;

import com.cryptory.be.chart.domain.Chart;
import com.cryptory.be.chart.repository.ChartRepository;
import com.cryptory.be.coin.domain.Coin;
import com.cryptory.be.coin.domain.CoinSymbol;
import com.cryptory.be.coin.domain.CoinSymbolEnum;
import com.cryptory.be.coin.repository.CoinRepository;
import com.cryptory.be.coin.repository.CoinSymbolRepository;
import com.cryptory.be.openapi.dto.Candle;
import com.cryptory.be.openapi.dto.Market;
import com.cryptory.be.openapi.service.UpbitService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitDataLoad {

    private final UpbitService upbitService;
    private final CoinRepository coinRepository;
    private final ChartRepository chartRepository;
    private final CoinSymbolRepository coinSymbolRepository;

    // 애플리케이션 시작 시 자동 db 저장
    @PostConstruct
    @Transactional // 단일 트랜잭션으로 실행
    public void fetchInitialData() {
        long startTime = System.currentTimeMillis();
        log.info("Starting initial data load (Refactored)...");


        // 1. CoinSymbol 준비하기 (Find or Create)
        log.info("Preparing Coin Symbols...");
        Map<String, CoinSymbol> coinSymbolMap = prepareCoinSymbols();

        // 2. Coin 정보 가져와서 저장
        log.info("Fetching and saving Coins...");
        Set<String> hiddenKeys = Set.of(
                "MTL", "XRP", "GRS", "IOST", "HI", "ONG", "CB", "ELF", "QTUM",
                "BTT", "MOC", "ARGO", "TT", "GMB", "MBL", "MLK", "STPT", "STMX", "DKA",
                "AHT", "BORA", "JST", "CRO", "TON", "SOLA", "HUNT", "DOT", "STRAT",
                "AQT", "GLM", "META", "FCT", "KOB", "SAND", "HPO", "STRK", "NPXS",
                "STX", "MATIC", "T", "GMT", "EGLD", "GRT", "BLUR"
        );
        List<Long> idsToDelete = Arrays.asList(1L, 2L, 3L, 6L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 32L, 33L, 34L, 37L, 40L, 41L, 42L, 46L, 47L, 48L, 50L, 54L, 55L, 56L, 59L, 60L, 61L, 64L, 65L, 67L, 68L, 72L, 73L, 76L, 77L, 81L, 82L, 85L, 86L, 87L, 88L, 89L, 91L, 92L, 94L, 95L, 96L, 97L, 98L, 100L, 102L, 103L, 104L, 105L, 107L, 110L);

        // 업비트에서 KRW로 거래되는 코인 목록 조회
        List<Market> coinsByUpbit = upbitService.getCoinsFromUpbit();

        // 코인 목록 저장
        List<Coin> coinsToSave = coinsByUpbit.stream()
                .map(market -> {
                    CoinSymbolEnum coinSymbolEnum = CoinSymbolEnum.fromMarket(market.getMarket());
                    // CoinSymbol Map에서 가져오기
                    CoinSymbol coinSymbol = coinSymbolMap.get(coinSymbolEnum.getCode());
                    if (coinSymbol == null) {
                        log.error("Critical error: Managed CoinSymbol not found in prepared map for code: {}. Skipping coin: {}", coinSymbolEnum.getCode(), market.getMarket());
                        return null;
                    }
                    boolean isDisplayed = !hiddenKeys.contains(coinSymbolEnum.getCode()); // 숨길 코인 확인

                    return Coin.builder()
                            .koreanName(market.getKoreanName())
                            .englishName(market.getEnglishName())
                            .code(market.getMarket())
                            .coinSymbol(coinSymbol)
                            .isDisplayed(false)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        coinRepository.saveAll(coinsToSave);
        log.info("Saved {} coins.", coinsToSave.size());

        // 이미지 꺠지는 ID들로 삭제
        if(!idsToDelete.isEmpty()){
            coinRepository.deleteCoinsByIdNotIn(idsToDelete);
            log.info("Deleted coins with specified IDs.");
        }
        coinRepository.updateCoinDisplaySettings();
        log.info("Updated coin display settings.");
        /*
         * 차트 데이터 가져오는 작업(원래는 저장되는 코인에 대한 차트를 모두 저장해야 함)
         * 하지만 batch 사용 안하고, MVP 개발이므로 인기 코인 임의 5개 선정
         * 원래는 모든 코인에 대한 차트 데이터 가져와야 함
         * 모든 코인에 대한 차트 데이터 가져와서 저장하는 건 지금 필요 없고, 양이 너무 많음
         */
        log.info("Fetching and saving charts for popular coins (Optimized)...");
        List<String> popularCoinCodes = Arrays.asList("KRW-BTC", "KRW-ETH", "KRW-DOGE", "KRW-XRP", "KRW-ADA");

        // 필요한 Coin 엔티티들을 한 번에 조회 (Map<"KRW-BTC", Coin객체>)
        Map<String, Coin> popularCoinMap = coinRepository.findByCodeIn(popularCoinCodes).stream()
                .collect(Collectors.toMap(Coin::getCode, Function.identity()));

        for (String coinCode : popularCoinCodes) {
            Coin coin = popularCoinMap.get(coinCode);
            if (coin == null) {
                log.warn("Coin not found in database for code: {}. Skipping chart data.", coinCode);
                continue;
            }

            List<Candle> candles = upbitService.getCharts(coinCode);
            if (candles == null || candles.isEmpty()) {
                log.info("No chart data found or failed to fetch for {}", coinCode);
                continue;
            }

            List<Chart> chartsToSave = candles.stream()
                    .map(candle -> Chart.builder()
                            .date(candle.getCandleDateTime())
                            .openingPrice(candle.getOpeningPrice())
                            .highPrice(candle.getHighPrice())
                            .lowPrice(candle.getLowPrice())
                            .tradePrice(candle.getTradePrice())
                            .changeRate(candle.getChangeRate())
                            .changePrice(candle.getChangePrice())
                            .coin(coin) // **미리 조회한 Coin 엔티티 사용**
                            .build())
                    .toList();

            chartRepository.saveAll(chartsToSave);
            log.info("Saved {} chart entries for {}", chartsToSave.size(), coinCode);
        }

        long endTime = System.currentTimeMillis();
        log.info("Initial data load 경과시간 >>> {} ms.", (endTime - startTime));
    }

    private Map<String, CoinSymbol> prepareCoinSymbols(){
        // 1. DB에서 현재 모든 CoinSymbol 조회
        Map<String, CoinSymbol> existingSymbols = coinSymbolRepository.findAll().stream()
                    .collect(Collectors.toMap(CoinSymbol::getCode, Function.identity()));

        // 2. CoinSymbolEnum 순회하며 DB에 없는거 찾아내기
        List<CoinSymbol> symbolsToSave = Arrays.stream(CoinSymbolEnum.values())
                .filter(enumVal -> !existingSymbols.containsKey(enumVal.getCode())) // DB에 없는 값들만 필터링
                .map(CoinSymbolEnum::toCoinSymbol) // 새 CoinSymbol 객체 생성
                .toList();

        // 3. 새로 생성해야 할 CoinSymbol 저장
        if(!symbolsToSave.isEmpty()){
            log.info("Saving {} new CoinSymbols to the database.", symbolsToSave.size());
            coinSymbolRepository.saveAll(symbolsToSave);

            symbolsToSave.forEach(savedSymbol -> existingSymbols.put(savedSymbol.getCode(), savedSymbol));
        }

        log.info("Prepared {} CoinSymbols in total.", existingSymbols.size());
        // Map 결과 반환하기
        return existingSymbols;
    }
}
