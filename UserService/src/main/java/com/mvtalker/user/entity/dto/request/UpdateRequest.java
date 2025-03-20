package com.mvtalker.user.entity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mvtalker.user.entity.enums.OnlineStatus;
import com.mvtalker.user.entity.enums.Platform;
import com.mvtalker.user.entity.enums.UserStatus;
import com.mvtalker.user.entity.enums.Visibility;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class UpdateRequest
{
    //@NotNull(message = "字段 id 不能为空")
    //private Long id;
    private String nickname;
    private String avatarUrl;
    private UserStatus status;
    private Visibility visibility;

    @NotBlank(message = "字段 deviceId 不能为空")
    private String deviceId;
    private String clientVersion;
    private LocalDateTime lastOnline;
    private OnlineStatus onlineStatus;
    private String ipGeo;
}
