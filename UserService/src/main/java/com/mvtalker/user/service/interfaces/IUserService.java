package com.mvtalker.user.service.interfaces;

import com.mvtalker.user.entity.dto.request.*;
import com.mvtalker.utilities.entity.dto.response.BaseResponse;
import com.mvtalker.user.entity.dto.response.LoginResponse;
import com.mvtalker.user.entity.dto.response.SearchResponse;

import javax.servlet.http.HttpServletRequest;

public interface IUserService
{
    BaseResponse<LoginResponse> login(LoginRequest loginRequest, HttpServletRequest httpRequest);
    BaseResponse<LoginResponse> register(RegisterRequest registerRequest, HttpServletRequest httpRequest);
    BaseResponse<SearchResponse> update(UpdateRequest updateRequest, HttpServletRequest httpRequest);
    BaseResponse<SearchResponse> search(String deviceId);
}

