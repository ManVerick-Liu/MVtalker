package com.mvtalker.user.service.interfaces;

import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.user.response.UserViewMultiResponse;

public interface IUserTestService
{
    BaseResponse<UserViewMultiResponse> getOnlineUserViewMulti();
}
