package com.mvtalker.utilities.entity.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mvtalker.utilities.common.LongToStringSerializer;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import com.mvtalker.utilities.entity.user.enums.AccountStatus;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import com.mvtalker.utilities.entity.user.enums.UserVisibility;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class UserStatusDTO
{
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @JsonSerialize(using = LongToStringSerializer.class)
    private Long userId;
    private UserVisibility visibility;
    private OnlineStatus onlineStatus;
    private AccountStatus accountStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastOnline;

    public UserStatusDTO(Long userId, UserVisibility visibility, OnlineStatus onlineStatus, AccountStatus accountStatus, LocalDateTime lastOnline)
    {
        this.userId = userId;
        this.visibility = visibility;
        this.onlineStatus = onlineStatus;
        this.accountStatus = accountStatus;
        this.lastOnline = lastOnline;
    }
    public UserStatusDTO()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
