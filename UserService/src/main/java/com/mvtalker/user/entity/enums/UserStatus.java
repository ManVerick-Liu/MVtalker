package com.mvtalker.user.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus
{
    DISABLED("DISABLED"),
    NORMAL("NORMAL"),
    INACTIVE("INACTIVE");

    @EnumValue // 标记数据库实际存储的字段
    private final String value;

    UserStatus(String value)
    {
        this.value = value;
    }

}
