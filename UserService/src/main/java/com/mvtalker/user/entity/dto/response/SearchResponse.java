package com.mvtalker.user.entity.dto.response;

import com.mvtalker.user.entity.dto.DeviceBaseDTO;
import com.mvtalker.user.entity.dto.DeviceLoginDTO;
import com.mvtalker.user.entity.dto.UserBaseDTO;
import lombok.Data;

@Data
public class SearchResponse
{
    private UserBaseDTO userInfo;
    private DeviceBaseDTO deviceInfo;

    public SearchResponse(UserBaseDTO userInfo, DeviceBaseDTO deviceInfo)
    {
        this.userInfo = userInfo;
        this.deviceInfo = deviceInfo;
    }
}
