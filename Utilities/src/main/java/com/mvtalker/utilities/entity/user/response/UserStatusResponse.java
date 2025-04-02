package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserInfoDTO;
import com.mvtalker.utilities.entity.user.dto.UserStatusDTO;
import lombok.Data;

@Data
public class UserStatusResponse
{
    private UserStatusDTO userStatus;

    public UserStatusResponse(UserStatusDTO userStatus)
    {
        this.userStatus = userStatus;
    }
}
