package com.mvtalker.utilities.entity.user.dto;

import com.mvtalker.utilities.entity.user.enums.Platform;
import lombok.Data;

@Data
public class UserDeviceLoginDTO
{
    private Long agentId;
    private String deviceId;
    //private Long userId;
    private Platform platform;
    private String clientVersion;
    private String ipGeo;

    public UserDeviceLoginDTO(Long agentId, String deviceId, Platform platform, String clientVersion, String ipGeo)
    {
        this.agentId = agentId;
        this.deviceId = deviceId;
        this.platform = platform;
        this.clientVersion = clientVersion;
        this.ipGeo = ipGeo;
    }
}
