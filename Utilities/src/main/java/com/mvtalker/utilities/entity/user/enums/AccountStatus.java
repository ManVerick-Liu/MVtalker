package com.mvtalker.utilities.entity.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AccountStatus
{
    DISABLED("DISABLED"),
    NORMAL("NORMAL"),
    INACTIVE("INACTIVE");

    @EnumValue // 标记数据库实际存储的字段
    private final String value;

    AccountStatus(String value)
    {
        this.value = value;
    }

}
