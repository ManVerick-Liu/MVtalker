package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserLocalVolumeDTO;
import lombok.Data;

import java.util.List;

@Data
public class UserLocalVolumeMultiResponse
{
    private List<UserLocalVolumeDTO> userLocalVolumes;

    public UserLocalVolumeMultiResponse(List<UserLocalVolumeDTO> userLocalVolumes)
    {
        this.userLocalVolumes = userLocalVolumes;
    }
    public UserLocalVolumeMultiResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
