package com.mvtalker.utilities.entity.community.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CommunityVisibility
{
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE");

    @EnumValue
    private final String value;

    CommunityVisibility (String value)
    {
        this.value = value;
    }
}
