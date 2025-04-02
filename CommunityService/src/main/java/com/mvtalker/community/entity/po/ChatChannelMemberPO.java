
package com.mvtalker.community.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("chat_channel_member")
public class ChatChannelMemberPO
{
    @TableId(value = "agent_id", type = IdType.ASSIGN_ID)
    private Long agentId;

    @TableField("chat_channel_id")
    private Long chatChannelId;

    @TableField("user_id")
    private Long userId;
}