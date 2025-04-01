package com.cryptory.be.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class AwsMetadataTemplate {

    private static final String META_URL = "http://169.254.169.254";
    private static final String TOKEN_TTL_HEADER = "X-aws-ec2-metadata-token-ttl-seconds";
    private static final String TOKEN_HEADER = "X-aws-ec2-metadata-token";
    private static final String TOKEN_TTL = "3600"; // 60 minutes
    private static final String VERSION = "latest";

    private final RestTemplate restTemplate = new RestTemplate();

    // Method to create token
    public String createToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOKEN_TTL_HEADER, TOKEN_TTL);
        HttpEntity<Object> request = new HttpEntity<>(null, headers);

        try {
            return restTemplate.exchange(
                    META_URL + "/" + VERSION + "/api/token",
                    HttpMethod.PUT,
                    request,
                    String.class
            ).getBody();
        } catch (Exception ex) {
            log.warn("AWS 토큰 정보를 생성하는데 실패했습니다.");
            return null;
        }
    }

    // Method to get public IP
    public String getPublicIp(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOKEN_HEADER, token);
        HttpEntity<Object> request = new HttpEntity<>(null, headers);

        try {
            return restTemplate.exchange(
                    META_URL + "/" + VERSION + "/meta-data/public-ipv4",
                    HttpMethod.GET,
                    request,
                    String.class
            ).getBody();
        } catch (Exception ex) {
            log.warn("AWS IP 메타 정보를 가져오는데 실패했습니다. IP가 내부 값으로 설정됩니다.");
            return null;
        }
    }
}