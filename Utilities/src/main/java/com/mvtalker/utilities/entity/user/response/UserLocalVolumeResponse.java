package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserLocalVolumeDTO;
import lombok.Data;

@Data
public class UserLocalVolumeResponse
{
    private UserLocalVolumeDTO userLocalVolume;

    public UserLocalVolumeResponse(UserLocalVolumeDTO userLocalVolume)
    {
        this.userLocalVolume = userLocalVolume;
    }
}
