package com.mvtalker.user.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mvtalker.user.entity.enums.UserStatus;
import com.mvtalker.user.entity.enums.Visibility;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("user")
public class UserPO
{
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("mobile")
    private String mobileEncrypted;

    // 响应中排除敏感字段
    // 即使使用DTO进行数据传输，保留该注解仍然是一种安全性的做法
    // 以防哪天写错了用了PO进行数据传输，也不会暴露用户密码
    @JsonIgnore
    @TableField("password_hash")
    private String passwordEncrypted;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("status")
    private UserStatus status;

    @TableField("visibility")
    private Visibility visibility;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

}
