package com.mvtalker.user.controller;

import com.mvtalker.utilities.entity.user.request.*;
import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.user.response.*;
import com.mvtalker.user.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/user")
@RequiredArgsConstructor // Lombok自动生成构造器，自动为final字段生成构造函数
public class UserController
{
    private final IUserService iUserService;

    @PostMapping(value = "/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody UserAuthInfoRequest userAuthInfoRequest)
    {
        BaseResponse<LoginResponse> response = iUserService.login(userAuthInfoRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping(value = "/register")
    public ResponseEntity<BaseResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest registerRequest)
    {
        BaseResponse<LoginResponse> response = iUserService.register(registerRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/offline")
    public ResponseEntity<BaseResponse<Void>> offline()
    {
        BaseResponse<Void> response = iUserService.offline();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update-user-base-info")
    public ResponseEntity<BaseResponse<UserInfoResponse>> updateUserBaseInfo(@Valid @RequestBody UserBaseInfoRequest userBaseInfoRequest)
    {
        BaseResponse<UserInfoResponse> response = iUserService.updateUserBaseInfo(userBaseInfoRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update-user-auth-info")
    public ResponseEntity<BaseResponse<UserInfoResponse>> updateUserAuthInfo(@Valid @RequestBody UserAuthInfoRequest userAuthInfoRequest)
    {
        BaseResponse<UserInfoResponse> response = iUserService.updateUserAuthInfo(userAuthInfoRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update-user-status")
    public ResponseEntity<BaseResponse<UserStatusResponse>> updateUserStatus(@Valid @RequestBody UserStatusRequest userStatusRequest)
    {
        BaseResponse<UserStatusResponse> response = iUserService.updateUserStatus(userStatusRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update-user-global-volume")
    public ResponseEntity<BaseResponse<UserGlobalVolumeResponse>> updateUserGlobalVolume(@Valid @RequestBody UserGlobalVolumeRequest userGlobalVolumeRequest)
    {
        BaseResponse<UserGlobalVolumeResponse> response = iUserService.updateUserGlobalVolume(userGlobalVolumeRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update-user-local-volume")
    public ResponseEntity<BaseResponse<UserLocalVolumeResponse>> updateUserLocalVolume(@Valid @RequestBody UserLocalVolumeRequest userLocalVolumeRequest)
    {
        BaseResponse<UserLocalVolumeResponse> response = iUserService.updateUserLocalVolume(userLocalVolumeRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-user-info-by-user-id")
    public ResponseEntity<BaseResponse<UserInfoResponse>> getUserInfoByUserId()
    {
        BaseResponse<UserInfoResponse> response = iUserService.getUserInfoByUserId();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-user-status-by-user-id")
    public ResponseEntity<BaseResponse<UserStatusResponse>> getUserStatusByUserId()
    {
        BaseResponse<UserStatusResponse> response = iUserService.getUserStatusByUserId();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping(value = "/get-user-views-by-user-ids")
    public ResponseEntity<BaseResponse<UserViewMultiResponse>> getUserViewMultiByUserIdMulti(@RequestBody UserIdMultiRequest userIdMultiRequest)
    {
        BaseResponse<UserViewMultiResponse> response = iUserService.getUserViewMultiByUserIdMulti(userIdMultiRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-user-view-by-user-id")
    public ResponseEntity<BaseResponse<UserViewResponse>> getUserViewByUserId(@RequestParam("userId") Long userId)
    {
        BaseResponse<UserViewResponse> response = iUserService.getUserViewByUserId(userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-user-global-volume-by-user-id")
    public ResponseEntity<BaseResponse<UserGlobalVolumeResponse>> getUserGlobalVolumeByUserId()
    {
        BaseResponse<UserGlobalVolumeResponse> response = iUserService.getUserGlobalVolumeByUserId();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-user-local-volume-by-user-id-and-community-id")
    public ResponseEntity<BaseResponse<UserLocalVolumeMultiResponse>> getUserLocalVolumeByUserIdAndCommunityId(@RequestParam("communityId") Long communityId)
    {
        BaseResponse<UserLocalVolumeMultiResponse> response = iUserService.getUserLocalVolumeByUserIdAndCommunityId(communityId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
