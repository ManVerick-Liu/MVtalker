package com.mvtalker.utilities.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service")
public interface UserFeignClient
{

}
