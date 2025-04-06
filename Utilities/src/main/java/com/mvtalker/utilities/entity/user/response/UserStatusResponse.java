package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserInfoDTO;
import com.mvtalker.utilities.entity.user.dto.UserStatusDTO;
import lombok.Data;

@Data
public class UserStatusResponse
{
    private UserStatusDTO userStatus;

    public UserStatusResponse(UserStatusDTO userStatus)
    {
        this.userStatus = userStatus;
    }
    public UserStatusResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
