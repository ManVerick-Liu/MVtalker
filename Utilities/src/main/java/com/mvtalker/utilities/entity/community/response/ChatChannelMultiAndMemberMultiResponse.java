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
    public ChatChannelMultiAndMemberMultiResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
