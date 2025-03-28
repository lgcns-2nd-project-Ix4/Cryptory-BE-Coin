package com.cryptory.be.openapi.client;

import com.cryptory.be.openapi.dto.Candle;
import com.cryptory.be.openapi.dto.Market;
import com.cryptory.be.openapi.dto.Ticker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpbitClient {

    private final static String UPBIT_API_URL = "https://api.upbit.com/v1/";
    private final static int BATCH_SIZE = 150;

    private final RestTemplate restTemplate;

    public List<Market> getCoins() {
        String url = UPBIT_API_URL + "market/all";

        return restTemplate.exchange(
                url,
                GET,
                null,
                new ParameterizedTypeReference<List<Market>>() {
                }
        ).getBody();
    }

    public List<Candle> getCharts(String market) {
        String today = LocalDateTime.now().toLocalDate().atStartOfDay().toString().replace("T", " ") + ":00";

        String url = UPBIT_API_URL + "candles/days?market=" + market + "&count=" + BATCH_SIZE + "&to=" + today;

        List<Candle> batch = getCandles(url);

        // 차트 300개 이하만 받아오기
        if (batch.size() == BATCH_SIZE) {
            String lastCandleTime = batch.get(batch.size() - 1).getCandleDateTime();
            url = UPBIT_API_URL + "candles/days?market=" + market + "&count=" + BATCH_SIZE + "&to=" + lastCandleTime;
            List<Candle> newBatch = getCandles(url);

            batch.addAll(newBatch);
        }

        // 모든 차트를 받아오기 위한 로직
//        while (batch.size() == BATCH_SIZE) {
//            // 마지막 봉의 'candle_date_time_utc'를 'to' 파라미터로 사용
//            String lastCandleTime = batch.get(BATCH_SIZE - 1).getCandleDateTime();
//            url = UPBIT_API_URL + "candles/days?market=" + market + "&count=" + BATCH_SIZE + "&to=" + lastCandleTime;
//
//            List<Candle> newBatch = getCandles(url);
//            if (newBatch.isEmpty()) {
//                break;  // 더 이상 데이터가 없으면 종료
//            }
//
//            // 새로운 데이터를 기존에 이어서 추가
//            batch.addAll(newBatch);
//        }

        return batch;
    }

    private List<Candle> getCandles(String url) {
        return restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Candle>>() {
                }
        ).getBody();
    }

    // 코인들 현재가 가져오기
    public List<Ticker> getTickers(String... market) {
        String url = UPBIT_API_URL + "ticker?markets=" + String.join(",", market);

        return restTemplate.exchange(
                url,
                GET,
                null,
                new ParameterizedTypeReference<List<Ticker>>() {
                }
        ).getBody();
    }
}
