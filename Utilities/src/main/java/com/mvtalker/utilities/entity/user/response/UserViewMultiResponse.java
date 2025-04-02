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
}
