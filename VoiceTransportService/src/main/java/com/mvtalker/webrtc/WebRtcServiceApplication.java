package com.mvtalker.webrtc;

import com.mvtalker.utilities.config.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients(basePackages = "com.mvtalker.utilities.feign", defaultConfiguration = FeignConfig.class)
@ComponentScan(basePackages = {"com.mvtalker.utilities", "com.mvtalker.webrtc"})
@SpringBootApplication
public class WebRtcServiceApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(WebRtcServiceApplication.class, args);
    }
}
