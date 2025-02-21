package com.mvtalker.utilities.config;

import com.mvtalker.utilities.common.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class FeignConfig
{
    // 配置Feign的日志级别
    @Bean
    public Logger.Level feignLoggerLevel()
    {
        return Logger.Level.BASIC;
    }

    // 配置Feign请求拦截器，用于微服务之间传递用户信息
    @Bean
    public RequestInterceptor userInfoRequestInterceptor()
    {
        return new RequestInterceptor()
        {
            @Override
            public void apply(feign.RequestTemplate requestTemplate)
            {
                requestTemplate.header("userInfo", UserContext.getUserContext());
            }
        };
    }
}
