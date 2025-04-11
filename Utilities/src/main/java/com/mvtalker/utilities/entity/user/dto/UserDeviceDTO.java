package com.mvtalker.utilities.entity.user.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mvtalker.utilities.common.LongToStringSerializer;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import com.mvtalker.utilities.entity.user.enums.Platform;
import lombok.Data;

@Data
public class UserDeviceDTO
{
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @JsonSerialize(using = LongToStringSerializer.class)
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
    public UserDeviceDTO()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
