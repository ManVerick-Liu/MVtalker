package com.mvtalker.utilities.feign.fallback;

import com.mvtalker.utilities.feign.UserFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserFeignClientFallback implements FallbackFactory<UserFeignClient>
{
    @Override
    public UserFeignClient create(Throwable cause)
    {
        return null;
    }
}
