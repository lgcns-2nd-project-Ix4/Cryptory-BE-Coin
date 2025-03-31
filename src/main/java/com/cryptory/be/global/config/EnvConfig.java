//package com.cryptory.be.global.config;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class EnvConfig {
//
//    @PostConstruct
//    public void init() {
//        // .env 파일 로드
//        Dotenv dotenv = Dotenv.load();
//
//        // .env에서 값을 가져와 시스템 환경 변수로 설정
//        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
//        log.debug("ACCESS_EXPIRATION: {}", System.getProperty("ACCESS_EXPIRATION"));
//    }
//}
