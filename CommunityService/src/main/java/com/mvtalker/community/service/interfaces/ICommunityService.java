package com.mvtalker.community.service.interfaces;

import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.community.request.*;
import com.mvtalker.utilities.entity.community.response.*;

public interface ICommunityService
{
    BaseResponse<CommunityInfoAndMemberViewMultiResponse> createCommunity(CreateCommunityRequest createCommunityRequest);
    BaseResponse<CommunityInfoResponse> joinCommunity(JoinCommunityRequest joinCommunityRequest);
    BaseResponse<Void> dismissCommunity(Long communityId);
    BaseResponse<CommunityInfoResponse> updateCommunityInfo(UpdateCommunityInfoRequest updateCommunityInfoRequest);
    BaseResponse<CommunityMemberViewResponse> updateCommunityMemberRole(UpdateCommunityMemberRoleRequest updateCommunityMemberRoleRequest);
    BaseResponse<CommunityEntireInfoResponse> getCommunityEntireInfoByCommunityId(Long communityId);
    BaseResponse<CommunityInfoMultiResponse> getCommunityInfosByUserId();
    BaseResponse<CommunityInfoResponse> getCommunityInfoByCommunityId(Long communityId);
    BaseResponse<CommunityMemberViewMultiResponse> getCommunityMembersByCommunityId(Long communityId);
    BaseResponse<ChatChannelResponse> createChatChannel(CreateChatChannelRequest createChatChannelRequest);
    BaseResponse<ChatChannelAndMemberMultiResponse> joinChatChannel(JoinChatChannelRequest joinChatChannelRequest);
    BaseResponse<Void> dismissChatChannel(Long chatChannelId, Long communityId);
    BaseResponse<ChatChannelResponse> updateChatChannel(ChatChannelRequest ChatChannelRequest);
    BaseResponse<ChatChannelMultiAndMemberMultiResponse> getChatChannelsAndMembersByCommunityId(Long communityId);
}
