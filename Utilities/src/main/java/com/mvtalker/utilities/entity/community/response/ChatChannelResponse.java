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

    public ChatChannelResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
