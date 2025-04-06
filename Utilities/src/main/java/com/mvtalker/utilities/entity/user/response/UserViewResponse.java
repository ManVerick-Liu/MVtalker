package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserViewDTO;
import lombok.Data;

@Data
public class UserViewResponse
{
    private UserViewDTO userView;

    public UserViewResponse(UserViewDTO userView)
    {
        this.userView = userView;
    }
    public UserViewResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
