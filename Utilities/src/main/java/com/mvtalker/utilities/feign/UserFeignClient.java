package com.mvtalker.utilities.feign;

import com.mvtalker.utilities.feign.fallback.UserFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", fallbackFactory = UserFeignClientFallback.class)
public interface UserFeignClient
{

}
