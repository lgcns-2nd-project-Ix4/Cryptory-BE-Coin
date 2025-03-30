package com.cryptory.be;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableJpaAuditing
public class BeApplication {
	public static void main(String[] args) {
		// 운영 환경이 아니면 .env 파일 로드
		String activeProfile = System.getenv("ENV_ACTIVE");
		if (activeProfile == null || !activeProfile.equals("NO")) {
			Dotenv dotenv = Dotenv.load();
			dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		}

		SpringApplication.run(BeApplication.class, args);
	}
}
