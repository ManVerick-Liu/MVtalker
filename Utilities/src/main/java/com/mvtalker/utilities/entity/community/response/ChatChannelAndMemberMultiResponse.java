package com.mvtalker.utilities.entity.community.response;

import com.mvtalker.utilities.entity.community.dto.ChatChannelDTO;
import com.mvtalker.utilities.entity.user.dto.UserViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class ChatChannelAndMemberMultiResponse
{
    private ChatChannelDTO chatChannels;
    private List<UserViewDTO> chatChannelMembers;

    public ChatChannelAndMemberMultiResponse(ChatChannelDTO chatChannels, List<UserViewDTO> chatChannelMembers)
    {
        this.chatChannels = chatChannels;
        this.chatChannelMembers = chatChannelMembers;
    }
    public ChatChannelAndMemberMultiResponse()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
