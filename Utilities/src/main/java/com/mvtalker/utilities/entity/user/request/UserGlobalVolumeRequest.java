package com.mvtalker.utilities.entity.user.request;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class UserGlobalVolumeRequest
{
    @Min(value = 0, message = "输出音量不能小于0")
    @Max(value = 200, message = "输出音量不能大于200")
    private Integer outputVolume;

    private Boolean outputActive;

    @Min(value = 0, message = "输入音量不能小于0")
    @Max(value = 200, message = "输入音量不能大于200")
    private Integer inputVolume;

    private Boolean inputActive;
}
