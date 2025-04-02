package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.CommunityMemberViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class CommunityMemberViewMultiResponse
{
    private List<CommunityMemberViewDTO> communityMemberViews;

    public CommunityMemberViewMultiResponse(List<CommunityMemberViewDTO> communityMemberViews)
    {
        this.communityMemberViews = communityMemberViews;
    }
}
