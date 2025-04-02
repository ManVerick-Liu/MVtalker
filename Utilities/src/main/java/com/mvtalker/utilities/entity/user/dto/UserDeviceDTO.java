package com.mvtalker.utilities.entity.user.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mvtalker.utilities.entity.user.enums.Platform;
import lombok.Data;

@Data
public class UserDeviceDTO
{
    private Long agentId;
    //private Long userId;
    private Platform platform;
    private String clientVersion;
    private String ipGeo;

    public UserDeviceDTO(Long agentId, Platform platform, String clientVersion, String ipGeo)
    {
        this.agentId = agentId;
        this.platform = platform;
        this.clientVersion = clientVersion;
        this.ipGeo = ipGeo;
    }
}
