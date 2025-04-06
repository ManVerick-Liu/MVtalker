
package com.mvtalker.utilities.entity.community.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
public class ChatChannelMemberDTO
{
    //private Long agentId;
    private Long chatChannelId;
    private Long userId;

    public ChatChannelMemberDTO(Long chatChannelId, Long userId)
    {
        this.chatChannelId = chatChannelId;
        this.userId = userId;
    }
    public ChatChannelMemberDTO()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}