package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.CommunityInfoDTO;
import lombok.Data;

@Data
public class CommunityInfoResponse
{
    private CommunityInfoDTO communityInfo;

    public CommunityInfoResponse(CommunityInfoDTO communityInfo)
    {
        this.communityInfo = communityInfo;
    }
}
