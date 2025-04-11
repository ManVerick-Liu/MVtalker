package com.mvtalker.utilities.entity.user.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserLocalVolumeRequest
{
    @NotNull(message = "目标用户ID不能为空")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long targetId;

    @Min(value = 0, message = "输入音量不能小于0")
    @Max(value = 200, message = "输入音量不能大于200")
    private Integer inputVolume;

    private Boolean inputActive;
}
