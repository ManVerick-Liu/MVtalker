package com.mvtalker.user.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OnlineStatus
{
    ONLINE("ONLINE"),
    AWAY("AWAY"),
    OFFLINE("OFFLINE");

    @EnumValue
    private final String value;

    OnlineStatus(String value)
    {
        this.value = value;
    }
}
