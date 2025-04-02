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
}
