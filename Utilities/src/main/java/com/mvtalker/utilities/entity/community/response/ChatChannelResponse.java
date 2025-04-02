package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.ChatChannelDTO;
import lombok.Data;

@Data
public class ChatChannelResponse
{
    private ChatChannelDTO chatChannel;

    public ChatChannelResponse(ChatChannelDTO chatChannel)
    {
        this.chatChannel = chatChannel;
    }
}
