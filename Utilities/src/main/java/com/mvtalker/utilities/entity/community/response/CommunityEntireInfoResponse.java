package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.ChatChannelAndMemberMultiDTO;
import com.mvtalker.utilities.entity.community.dto.CommunityInfoDTO;
import com.mvtalker.utilities.entity.community.dto.CommunityMemberViewDTO;
import com.mvtalker.utilities.entity.user.dto.UserViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class CommunityEntireInfoResponse
{
    private CommunityInfoDTO communityInfo;
    private List<CommunityMemberViewDTO> communityMemberViews;
    private List<ChatChannelAndMemberMultiDTO> chatChannels;

    public CommunityEntireInfoResponse(CommunityInfoDTO communityInfo, List<CommunityMemberViewDTO> communityMemberViews, List<ChatChannelAndMemberMultiDTO> chatChannels)
    {
        this.communityInfo = communityInfo;
        this.communityMemberViews = communityMemberViews;
        this.chatChannels = chatChannels;
    }
}
