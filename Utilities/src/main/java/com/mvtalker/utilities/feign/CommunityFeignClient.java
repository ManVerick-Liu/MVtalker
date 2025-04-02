package com.mvtalker.utilities.feign;

import com.mvtalker.utilities.feign.fallback.CommunityFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "community-service", fallbackFactory = CommunityFeignClientFallback.class)
public interface CommunityFeignClient
{

}
