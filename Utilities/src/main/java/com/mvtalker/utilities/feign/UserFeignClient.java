package com.mvtalker.utilities.feign;

import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.user.request.UserIdMultiRequest;
import com.mvtalker.utilities.entity.user.response.UserViewMultiResponse;
import com.mvtalker.utilities.entity.user.response.UserViewResponse;
import com.mvtalker.utilities.feign.fallback.UserFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "user-service", fallbackFactory = UserFeignClientFallback.class)
public interface UserFeignClient
{
    @PostMapping(value = "/user/get-user-views-by-user-ids")
    ResponseEntity<BaseResponse<UserViewMultiResponse>> getUserViewMultiByUserIdMulti(@RequestBody UserIdMultiRequest userIdMultiRequest);

    @GetMapping(value = "/user/get-user-view-by-user-id")
    ResponseEntity<BaseResponse<UserViewResponse>> getUserViewByUserId(@RequestParam("userId") Long userId);
}
