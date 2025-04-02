
package com.mvtalker.community.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_channel")
public class ChatChannelPO
{
    @TableId(value = "chat_channel_id", type = IdType.ASSIGN_ID)
    private Long chatChannelId;

    @TableField("community_id")
    private Long communityId;

    @TableField("name")
    private String name;

    @TableField("capacity")
    private Integer capacity;

    @TableField("created_at")
    private LocalDateTime createdAt;
}