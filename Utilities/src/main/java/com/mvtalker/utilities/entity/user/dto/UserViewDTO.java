package com.mvtalker.utilities.entity.user.dto;

import com.mvtalker.utilities.entity.community.enums.MemberRole;
import com.mvtalker.utilities.entity.user.enums.AccountStatus;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserViewDTO
{
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private OnlineStatus onlineStatus;
    private AccountStatus accountStatus;
    private LocalDateTime lastOnline;

    public UserViewDTO(Long userId, String nickname, String avatarUrl, OnlineStatus onlineStatus, AccountStatus accountStatus, LocalDateTime lastOnline)
    {
        this.userId = userId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.onlineStatus = onlineStatus;
        this.accountStatus = accountStatus;
        this.lastOnline = lastOnline;
    }
    public UserViewDTO()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
