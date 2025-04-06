package com.mvtalker.community.controller;

import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.utilities.entity.community.request.*;
import com.mvtalker.utilities.entity.community.response.*;
import com.mvtalker.community.service.interfaces.ICommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/community")
@RequiredArgsConstructor
public class CommunityController
{
    private final ICommunityService communityService;

    @PostMapping(value = "/create-community")
    public ResponseEntity<BaseResponse<CommunityInfoAndMemberViewMultiResponse>> createCommunity(@Valid @RequestBody CreateCommunityRequest createCommunityRequest)
    {
        BaseResponse<CommunityInfoAndMemberViewMultiResponse> response = communityService.createCommunity(createCommunityRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping(value = "/join-community")
    public ResponseEntity<BaseResponse<CommunityInfoResponse>> joinCommunity(@Valid @RequestBody JoinCommunityRequest joinCommunityRequest) 
    {
        BaseResponse<CommunityInfoResponse> response = communityService.joinCommunity(joinCommunityRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping(value = "/dismiss-community")
    public ResponseEntity<BaseResponse<Void>> dismissCommunity(@RequestParam("communityId") Long communityId) 
    {
        BaseResponse<Void> response = communityService.dismissCommunity(communityId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update-community-info")
    public ResponseEntity<BaseResponse<CommunityInfoResponse>> updateCommunityInfo(@Valid @RequestBody UpdateCommunityInfoRequest updateCommunityInfoRequest) 
    {
        BaseResponse<CommunityInfoResponse> response = communityService.updateCommunityInfo(updateCommunityInfoRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update-community-member-role")
    public ResponseEntity<BaseResponse<CommunityMemberViewResponse>> updateCommunityMemberRole(@Valid @RequestBody UpdateCommunityMemberRoleRequest updateCommunityMemberRoleRequest)
    {
        BaseResponse<CommunityMemberViewResponse> response = communityService.updateCommunityMemberRole(updateCommunityMemberRoleRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-community-entire-info-by-community-id")
    public ResponseEntity<BaseResponse<CommunityEntireInfoResponse>> getCommunityEntireInfoByCommunityId(@RequestParam("communityId") Long communityId)
    {
        BaseResponse<CommunityEntireInfoResponse> response = communityService.getCommunityEntireInfoByCommunityId(communityId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-community-infos-by-user-id")
    public ResponseEntity<BaseResponse<CommunityInfoMultiResponse>> getCommunityInfosByUserId() 
    {
        BaseResponse<CommunityInfoMultiResponse> response = communityService.getCommunityInfosByUserId();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-community-info-by-community-id")
    public ResponseEntity<BaseResponse<CommunityInfoResponse>> getCommunityInfoByCommunityId(@RequestParam("communityId") Long communityId)
    {
        BaseResponse<CommunityInfoResponse> response = communityService.getCommunityInfoByCommunityId(communityId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-community-members-by-community-id")
    public ResponseEntity<BaseResponse<CommunityMemberViewMultiResponse>> getCommunityMembersByCommunityId(@RequestParam("communityId") Long communityId)
    {
        BaseResponse<CommunityMemberViewMultiResponse> response = communityService.getCommunityMembersByCommunityId(communityId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping(value = "/create-chat-channel")
    public ResponseEntity<BaseResponse<ChatChannelResponse>> createChatChannel(@Valid @RequestBody CreateChatChannelRequest createChatChannelRequest) 
    {
        BaseResponse<ChatChannelResponse> response = communityService.createChatChannel(createChatChannelRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping(value = "/join-chat-channel")
    public ResponseEntity<BaseResponse<ChatChannelAndMemberMultiResponse>> joinChatChannel(@Valid @RequestBody ChatChannelIdRequest chatChannelIdRequest)
    {
        BaseResponse<ChatChannelAndMemberMultiResponse> response = communityService.joinChatChannel(chatChannelIdRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping(value = "/leave-chat-channel")
    public ResponseEntity<BaseResponse<Void>> leaveChatChannel(@Valid @RequestBody ChatChannelIdRequest chatChannelIdRequest)
    {
        BaseResponse<Void> response = communityService.leaveChatChannel(chatChannelIdRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping(value = "/dismiss-chat-channel")
    public ResponseEntity<BaseResponse<Void>> dismissChatChannel(
            @RequestParam("chatChannelId") Long chatChannelId,
            @RequestParam("communityId") Long communityId) 
    {
        BaseResponse<Void> response = communityService.dismissChatChannel(chatChannelId, communityId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping(value = "/update-chat-channel")
    public ResponseEntity<BaseResponse<ChatChannelResponse>> updateChatChannel(@Valid @RequestBody ChatChannelRequest chatChannelRequest) 
    {
        BaseResponse<ChatChannelResponse> response = communityService.updateChatChannel(chatChannelRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/get-chat-channels-and-chat-channel-members-by-community-id")
    public ResponseEntity<BaseResponse<ChatChannelMultiAndMemberMultiResponse>> getChatChannelsAndMembersByCommunityId(@RequestParam("communityId") Long communityId)
    {
        BaseResponse<ChatChannelMultiAndMemberMultiResponse> response = communityService.getChatChannelsAndMembersByCommunityId(communityId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
