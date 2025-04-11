package com.mvtalker.user.controller;

import com.mvtalker.user.service.UserTestService;
import com.mvtalker.user.service.interfaces.IUserTestService;
import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.user.response.LoginResponse;
import com.mvtalker.utilities.entity.user.response.UserViewMultiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user/test")
@RequiredArgsConstructor
public class UserTestController
{
    private final IUserTestService iUserTestService;

    @GetMapping(value = "/get-online-user-views")
    public ResponseEntity<BaseResponse<UserViewMultiResponse>> getOnlineUserViewMulti()
    {
        BaseResponse<UserViewMultiResponse> response = iUserTestService.getOnlineUserViewMulti();
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
