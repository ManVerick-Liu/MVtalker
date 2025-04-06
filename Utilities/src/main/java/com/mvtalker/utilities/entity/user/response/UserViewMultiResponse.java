package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserLocalVolumeDTO;
import com.mvtalker.utilities.entity.user.dto.UserViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class UserViewMultiResponse
{
    private List<UserViewDTO> userViews;

    public UserViewMultiResponse(List<UserViewDTO> userViews)
    {
        this.userViews = userViews;
    }
    public UserViewMultiResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
