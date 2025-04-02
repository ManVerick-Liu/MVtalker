package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.ChatChannelAndMemberMultiDTO;
import lombok.Data;

import java.util.List;

@Data
public class ChatChannelMultiAndMemberMultiResponse
{
    private List<ChatChannelAndMemberMultiDTO> chatChannels;

    public ChatChannelMultiAndMemberMultiResponse(List<ChatChannelAndMemberMultiDTO> chatChannels)
    {
        this.chatChannels = chatChannels;
    }
}
