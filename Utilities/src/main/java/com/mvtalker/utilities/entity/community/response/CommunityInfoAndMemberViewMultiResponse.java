package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.CommunityInfoDTO;
import com.mvtalker.utilities.entity.community.dto.CommunityMemberViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class CommunityInfoAndMemberViewMultiResponse
{
    private CommunityInfoDTO communityInfo;
    private List<CommunityMemberViewDTO> communityMemberViews;

    public CommunityInfoAndMemberViewMultiResponse(CommunityInfoDTO communityInfo, List<CommunityMemberViewDTO> communityMemberViews)
    {
        this.communityInfo = communityInfo;
        this.communityMemberViews = communityMemberViews;
    }
}
