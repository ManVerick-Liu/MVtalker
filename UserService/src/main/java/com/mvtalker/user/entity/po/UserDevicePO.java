package com.mvtalker.user.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import com.mvtalker.utilities.entity.user.enums.Platform;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_device")
public class UserDevicePO
{
    @TableId(value = "agent_id", type = IdType.ASSIGN_ID)
    private Long agentId;

    @TableField(value = "device_id")
    private String deviceId;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "platform")
    private Platform platform;

    @TableField(value = "client_version")
    private String clientVersion;

    @TableField(value = "ip_geo")
    private String ipGeo;
}
