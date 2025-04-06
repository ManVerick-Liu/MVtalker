
package com.mvtalker.utilities.entity.community.dto;

import com.mvtalker.utilities.entity.user.dto.UserViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class ChatChannelAndMemberMultiDTO
{
    private Long chatChannelId;
    private Long communityId;
    private String name;
    private Integer capacity;
    private List<UserViewDTO> chatChannelMembers;

    public ChatChannelAndMemberMultiDTO(Long chatChannelId, Long communityId, String name, Integer capacity, List<UserViewDTO> chatChannelMembers)
    {
        this.chatChannelId = chatChannelId;
        this.communityId = communityId;
        this.name = name;
        this.capacity = capacity;
        this.chatChannelMembers = chatChannelMembers;
    }
    public ChatChannelAndMemberMultiDTO()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}