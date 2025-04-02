
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
}