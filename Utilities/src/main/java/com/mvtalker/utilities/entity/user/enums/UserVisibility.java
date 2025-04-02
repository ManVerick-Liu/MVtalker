package com.mvtalker.utilities.entity.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserVisibility
{
    PUBLIC("PUBLIC"),
    STEALTH("STEALTH");

    @EnumValue
    private final String value;

    UserVisibility(String value)
    {
        this.value = value;
    }

}
