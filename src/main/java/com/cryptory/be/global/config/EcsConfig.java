package com.cryptory.be.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
@Profile("docker")
public class EcsConfig {
    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfigBean(InetUtils inetUtils) {
        EurekaInstanceConfigBean config = new EurekaInstanceConfigBean(inetUtils);
        String ip = null;
        try{
            ip = InetAddress.getLocalHost().getHostAddress();
            log.info("ECS Task Container Private Ip address is {}", ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        config.setIpAddress(ip);
        config.setPreferIpAddress(true);
        return config;
    }
}
