package com.mvtalker.utilities.entity.user.request;

import com.mvtalker.utilities.entity.user.enums.Platform;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class RegisterRequest
{
    @NotBlank(message = "字段 mobile 不能为空")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "字段 mobile 必须符合E.164格式")
    private String mobile;

    @NotBlank(message = "字段 password 不能为空")
    private String password;

    @NotBlank(message = "字段 nickname 不能为空")
    private String nickname;

    private String avatarUrl;

    /*
    @NotNull(message = "字段 platform 不能为空")
    private Platform platform;

    @NotBlank(message = "字段 clientVersion 不能为空")
    private String clientVersion;
    */
}
