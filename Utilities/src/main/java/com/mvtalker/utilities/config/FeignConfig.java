package com.mvtalker.utilities.config;

import com.mvtalker.utilities.common.GlobalConstantValue;
import com.mvtalker.utilities.common.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
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
        return requestTemplate ->
        {
            Long userId = UserContext.getUserId();
            if(userId != null)
            {
                requestTemplate.header(GlobalConstantValue.USER_CONTEXT_ID_HEADER_NAME, userId.toString());
            }
            else log.error("userId为空，无法将userId存入Feign发起的请求");
        };
    }
}
