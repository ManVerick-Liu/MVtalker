package com.mvtalker.utilities.entity.user.response;

import com.mvtalker.utilities.entity.user.dto.UserGlobalVolumeDTO;
import lombok.Data;

@Data
public class UserGlobalVolumeResponse
{
    private UserGlobalVolumeDTO userGlobalVolume;

    public UserGlobalVolumeResponse(UserGlobalVolumeDTO userGlobalVolume)
    {
        this.userGlobalVolume = userGlobalVolume;
    }
}
