package com.mvtalker.utilities.entity.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mvtalker.utilities.entity.user.enums.AccountStatus;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import com.mvtalker.utilities.entity.user.enums.UserVisibility;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class UserStatusDTO
{
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
}
