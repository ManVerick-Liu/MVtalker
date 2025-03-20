package com.mvtalker.user.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import com.mvtalker.user.entity.enums.OnlineStatus;
import com.mvtalker.user.entity.enums.Platform;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@TableName("device")
public class DevicePO
{
    // 复合主键声明
    @TableId(value = "device_id", type = IdType.INPUT)
    private String deviceId;

    @TableField(value = "_id")
    private Long userId;

    @TableField("platform")
    private Platform platform;

    @TableField("client_version")
    private String clientVersion;

    @TableField(value = "last_online")
    private LocalDateTime lastOnline;

    @TableField("online_status")
    private OnlineStatus onlineStatus;

    @TableField("ip_geo")
    private String ipGeo;

    // 关联用户信息（非数据库字段）
    @TableField(exist = false)
    private UserPO user;
}
