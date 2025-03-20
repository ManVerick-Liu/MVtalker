package com.mvtalker.user.entity.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mvtalker.user.entity.enums.UserStatus;
import com.mvtalker.user.entity.enums.Visibility;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBaseDTO
{
    // 基础信息
    private Long id;
    private String mobile;
    private String nickname;
    private String avatarUrl;

    // 状态信息
    private UserStatus status;
    private Visibility visibility;

    // 时间信息（序列化格式化）
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

}
