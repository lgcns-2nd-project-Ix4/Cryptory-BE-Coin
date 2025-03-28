package com.cryptory.be.global.config;

import com.cryptory.be.global.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

// private final TrafficLogInterceptor trafficLogInterceptor;

//    @Override
//    public void addInterceptors(InterceptorRegistry registry){
//        registry.addInterceptor(trafficLogInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/css/**", "/images/**", "/js/**", "/error");
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = FileUtils.getBaseDir();

        registry.addResourceHandler("/attach/files/**")
                .addResourceLocations("file://" + baseDir + "/");

    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
