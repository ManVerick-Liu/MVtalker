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
    public CommunityEntireInfoResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
