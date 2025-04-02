package com.mvtalker.utilities.entity.community.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MemberRole
{
    OWNER("OWNER"),
    ADMIN("ADMIN"),
    MEMBER("MEMBER");

    @EnumValue
    private final String value;

    MemberRole(String value)
    {
        this.value = value;
    }
}
