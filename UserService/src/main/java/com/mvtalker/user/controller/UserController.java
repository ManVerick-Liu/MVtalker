package com.mvtalker.user.controller;

import com.mvtalker.user.entity.dto.request.LoginRequest;
import com.mvtalker.user.entity.dto.request.RegisterRequest;
import com.mvtalker.user.entity.dto.request.UpdateRequest;
import com.mvtalker.utilities.entity.dto.response.BaseResponse;
import com.mvtalker.user.entity.dto.response.LoginResponse;
import com.mvtalker.user.entity.dto.response.SearchResponse;
import com.mvtalker.user.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping(value = "/user")
@RequiredArgsConstructor // Lombok自动生成构造器，自动为final字段生成构造函数
public class UserController
{
    private final IUserService iUserService;

    @PostMapping(value = "/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest httpRequest)
    {
        BaseResponse<LoginResponse> response = iUserService.login(loginRequest, httpRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping(value = "/register")
    public ResponseEntity<BaseResponse<LoginResponse>> register(@RequestBody RegisterRequest registerRequest, HttpServletRequest httpRequest)
    {
        BaseResponse<LoginResponse> response = iUserService.register(registerRequest, httpRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update")
    public ResponseEntity<BaseResponse<SearchResponse>> update(@RequestBody UpdateRequest updateRequest, HttpServletRequest httpRequest)
    {
        BaseResponse<SearchResponse> response = iUserService.update(updateRequest, httpRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<BaseResponse<SearchResponse>> search(@RequestParam("deviceId") @NotBlank String deviceId)
    {
        BaseResponse<SearchResponse> response = iUserService.search(deviceId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
