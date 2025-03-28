package com.cryptory.be.openapi.client;

import com.cryptory.be.openapi.dto.NaverNews;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverClient {

    private final RestTemplate restTemplate;

    //@Value("${naver.client-id}")
    private String clientId = "jmN_6X059uBGP0khRplV";

    //@Value("${naver.secret}")
    private String secret = "r6IvZUUTea";

    private final static String NAVER_NEWS_URL = "https://openapi.naver.com/v1/search/news.json";
    private final static int DISPLAY = 10;
    private final static int START = 1;
    private final static String SORT = "sim"; // "date" 날짜순


    public List<NaverNews> getNaverNewsWithWord(String coinName) {
        String requestUrl = NAVER_NEWS_URL+ "?query=" + URLEncoder.encode(coinName, StandardCharsets.UTF_8)
                + "&display=" + DISPLAY + "&start=" + START + "&sort=" + SORT;

        log.info("clientId: {}", clientId);
        log.info("secret: {}", secret);

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", secret);
        String responseBody = get(requestUrl, requestHeaders);

        return parseItemsFromJson(responseBody);
    }

    // 지피티 코드 참고
    private List<NaverNews> parseItemsFromJson(String responseBody) {
        // Gson 객체 생성

        Gson gson = new Gson();

        // JSON 응답을 JsonObject로 파싱
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

        // "items" 필드를 JsonArray로 추출


        JsonArray itemsArray = jsonObject.getAsJsonArray("items");

        // JsonArray를 List<NaverNews>로 변환
        return gson.fromJson(itemsArray, new TypeToken<List<NaverNews>>(){}.getType());
    }

    private static String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }


            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 오류 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }


    private static HttpURLConnection connect(String requestUrl){
        try {
            URL url = new URL(requestUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + requestUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + requestUrl, e);
        }
    }


    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);


        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();


            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }


            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }
}
