package com.mvtalker.utilities.entity.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Platform
{
    iOS("iOS"),
    Android("Android"),
    Web("Web"),
    Windows("Windows"),
    macOS("macOS");

    @EnumValue
    private final String value;

    Platform(String value)
    {
        this.value = value;
    }
}
