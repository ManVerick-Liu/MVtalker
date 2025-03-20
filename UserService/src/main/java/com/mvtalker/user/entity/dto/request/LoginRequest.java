package com.mvtalker.user.entity.dto.request;

import com.mvtalker.user.entity.enums.Platform;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest
{
    @NotBlank(message = "字段 mobile 不能为空")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "字段 mobile 必须符合E.164格式")
    private String mobile;

    @NotBlank(message = "字段 password 不能为空")
    private String password;

    @NotNull(message = "字段 platform 不能为空")
    private Platform platform;

    @NotBlank(message = "字段 clientVersion 不能为空")
    private String clientVersion;
}
