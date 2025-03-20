package com.mvtalker.gateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mvtalker.gateway", "com.mvtalker.utilities"})
public class GatewayApplication
{
    public static void main(String[] args)
    {
        org.springframework.boot.SpringApplication.run(GatewayApplication.class, args);
    }
}
