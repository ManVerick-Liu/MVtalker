package com.mvtalker.user.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Visibility
{
    PUBLIC("PUBLIC"),
    STEALTH("STEALTH");

    @EnumValue
    private final String value;

    Visibility(String value)
    {
        this.value = value;
    }

}
