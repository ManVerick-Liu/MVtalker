
package com.mvtalker.utilities.entity.community.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class ChatChannelDTO
{
    private Long chatChannelId;
    private Long communityId;
    private String name;
    private Integer capacity;

    public ChatChannelDTO(Long chatChannelId, Long communityId, String name, Integer capacity)
    {
        this.chatChannelId = chatChannelId;
        this.communityId = communityId;
        this.name = name;
        this.capacity = capacity;
    }
}