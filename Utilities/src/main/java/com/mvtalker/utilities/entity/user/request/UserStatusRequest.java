package com.mvtalker.utilities.entity.user.request;

import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import com.mvtalker.utilities.entity.user.enums.UserVisibility;
import lombok.Data;

@Data
public class UserStatusRequest
{
    private UserVisibility visibility;
    private OnlineStatus onlineStatus;
}
