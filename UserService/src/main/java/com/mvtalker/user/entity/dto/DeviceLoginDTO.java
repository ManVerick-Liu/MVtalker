package com.mvtalker.user.entity.dto;

import com.mvtalker.user.entity.enums.OnlineStatus;
import com.mvtalker.user.entity.enums.Platform;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceLoginDTO
{
    private String deviceId;
    private Platform platform;
    private String clientVersion;
    private LocalDateTime lastOnline;
    private OnlineStatus onlineStatus;
    private String ipGeo;
}
