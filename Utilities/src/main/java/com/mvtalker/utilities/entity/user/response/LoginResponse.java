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
    public LoginResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
