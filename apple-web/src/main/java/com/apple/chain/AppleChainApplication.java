package com.apple.chain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Application entry point for 苹果产业链云服务平台.
 * Spring Security auto-configuration excluded; JWT interceptor handles auth.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.apple.chain.**.mapper")
public class AppleChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppleChainApplication.class, args);
    }
}
