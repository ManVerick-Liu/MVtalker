package com.mvtalker.user.entity.dto.response;

import com.mvtalker.user.entity.dto.DeviceLoginDTO;
import com.mvtalker.user.entity.dto.UserBaseDTO;
import lombok.Data;

@Data
public class LoginResponse
{
    private String token;
    private UserBaseDTO userInfo;
    private DeviceLoginDTO deviceInfo;

    public LoginResponse(String token, UserBaseDTO userInfo, DeviceLoginDTO deviceInfo)
    {
        this.token = token;
        this.userInfo = userInfo;
        this.deviceInfo = deviceInfo;
    }
}
