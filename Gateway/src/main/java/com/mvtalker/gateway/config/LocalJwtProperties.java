package com.mvtalker.gateway.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Configuration
@ConfigurationProperties(prefix = "gateway.jwt")
public class LocalJwtProperties
{
    private List<String> excludePaths;

    public void setExcludePaths(List<String> excludePaths)
    {
        this.excludePaths = excludePaths;
    }
}
