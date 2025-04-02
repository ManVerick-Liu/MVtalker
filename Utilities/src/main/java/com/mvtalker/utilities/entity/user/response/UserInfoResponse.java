package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserInfoDTO;
import lombok.Data;

@Data
public class UserInfoResponse
{
    private UserInfoDTO userInfo;

    public UserInfoResponse(UserInfoDTO userInfo)
    {
        this.userInfo = userInfo;
    }
}
