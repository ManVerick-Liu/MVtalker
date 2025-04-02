package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserGlobalVolumeDTO;
import com.mvtalker.utilities.entity.user.dto.UserInfoDTO;
import com.mvtalker.utilities.entity.user.dto.UserStatusDTO;
import lombok.Data;

@Data
public class LoginResponse
{
    private String token;
    private UserInfoDTO userInfo;
    private UserStatusDTO userStatus;
    private UserGlobalVolumeDTO userGlobalVolume;

    public LoginResponse(String token, UserInfoDTO userInfo, UserStatusDTO userStatus, UserGlobalVolumeDTO userGlobalVolume)
    {
        this.token = token;
        this.userInfo = userInfo;
        this.userStatus = userStatus;
        this.userGlobalVolume = userGlobalVolume;
    }
}
