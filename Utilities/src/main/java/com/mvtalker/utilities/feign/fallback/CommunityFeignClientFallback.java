package com.mvtalker.utilities.feign.fallback;

import com.mvtalker.utilities.feign.CommunityFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CommunityFeignClientFallback implements FallbackFactory<CommunityFeignClient>
{
    @Override
    public CommunityFeignClient create(Throwable cause)
    {
        return null;
    }
}
