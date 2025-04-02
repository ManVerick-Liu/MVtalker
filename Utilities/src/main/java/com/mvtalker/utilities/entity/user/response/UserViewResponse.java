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
}
