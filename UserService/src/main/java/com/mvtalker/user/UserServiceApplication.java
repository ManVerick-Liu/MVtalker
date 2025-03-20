package com.mvtalker.user;

import com.mvtalker.utilities.config.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients(basePackages = "com.mvtalker.utilities.feign", defaultConfiguration = FeignConfig.class)
@ComponentScan(basePackages = {"com.mvtalker.utilities", "com.mvtalker.user"})
@SpringBootApplication
public class UserServiceApplication
{
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
