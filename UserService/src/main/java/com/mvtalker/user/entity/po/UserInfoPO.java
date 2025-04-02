package com.mvtalker.user.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("user_info")
public class UserInfoPO
{
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;

    @TableField(value = "mobile")
    private String mobileEncrypted;

    // 响应中排除敏感字段
    // 即使使用DTO进行数据传输，保留该注解仍然是一种安全性的做法
    // 以防哪天写错了用了PO进行数据传输，也不会暴露用户密码
    @JsonIgnore
    @TableField(value = "password_hash")
    private String passwordEncrypted;

    @TableField(value = "nickname")
    private String nickname;

    @TableField(value = "avatar_url")
    private String avatarUrl;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
