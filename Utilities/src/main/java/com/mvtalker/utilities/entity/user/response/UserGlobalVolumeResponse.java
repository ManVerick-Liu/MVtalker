package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserGlobalVolumeDTO;
import lombok.Data;

@Data
public class UserGlobalVolumeResponse
{
    private UserGlobalVolumeDTO userGlobalVolume;

    public UserGlobalVolumeResponse(UserGlobalVolumeDTO userGlobalVolume)
    {
        this.userGlobalVolume = userGlobalVolume;
    }

    public UserGlobalVolumeResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
