package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.CommunityMemberViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class CommunityMemberViewResponse
{
    private CommunityMemberViewDTO communityMemberView;

    public CommunityMemberViewResponse(CommunityMemberViewDTO communityMemberView)
    {
        this.communityMemberView = communityMemberView;
    }
}
