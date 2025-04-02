package com.mvtalker.user.service.interfaces;

import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.user.request.*;
import com.mvtalker.utilities.entity.user.response.*;

import java.util.List;

public interface IUserService
{
    BaseResponse<LoginResponse> login(UserAuthInfoRequest userAuthInfoRequest);
    BaseResponse<LoginResponse> register(RegisterRequest registerRequest);
    BaseResponse<Void> offline();
    BaseResponse<UserInfoResponse> updateUserBaseInfo(UserBaseInfoRequest userBaseInfoRequest);
    BaseResponse<UserInfoResponse> updateUserAuthInfo(UserAuthInfoRequest userAuthInfoRequest);
    BaseResponse<UserStatusResponse> updateUserStatus(UserStatusRequest userStatusRequest);
    BaseResponse<UserGlobalVolumeResponse> updateUserGlobalVolume(UserGlobalVolumeRequest userGlobalVolumeRequest);
    BaseResponse<UserLocalVolumeResponse> updateUserLocalVolume(UserLocalVolumeRequest userLocalVolumeRequest);
    BaseResponse<UserInfoResponse> getUserInfoByUserId();
    BaseResponse<UserStatusResponse> getUserStatusByUserId();
    BaseResponse<UserViewMultiResponse> getUserViewMultiByUserIdMulti(UserIdMultiRequest userIdMultiRequest);
    BaseResponse<UserViewResponse> getUserViewByUserId(Long userId);
    BaseResponse<UserGlobalVolumeResponse> getUserGlobalVolumeByUserId();
    BaseResponse<UserLocalVolumeMultiResponse> getUserLocalVolumeByUserIdAndCommunityId(Long communityId);
}

