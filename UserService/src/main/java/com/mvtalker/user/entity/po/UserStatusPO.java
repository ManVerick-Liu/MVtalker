package com.mvtalker.user.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mvtalker.utilities.entity.user.enums.AccountStatus;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import com.mvtalker.utilities.entity.user.enums.UserVisibility;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("user_status")
public class UserStatusPO
{
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;

    @TableField(value = "visibility")
    private UserVisibility visibility;

    @TableField(value = "online_status")
    private OnlineStatus onlineStatus;

    @TableField(value = "account_status")
    private AccountStatus accountStatus;

    @TableField(value = "last_online")
    private LocalDateTime lastOnline;
}
