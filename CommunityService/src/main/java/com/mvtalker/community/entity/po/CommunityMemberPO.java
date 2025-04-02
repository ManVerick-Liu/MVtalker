
package com.mvtalker.community.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mvtalker.utilities.entity.community.enums.MemberRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("community_member")
public class CommunityMemberPO
{
    @TableId(value = "agent_id", type = IdType.ASSIGN_ID)
    private Long agentId;

    @TableField("community_id")
    private Long communityId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private MemberRole role;

    @TableField("join_time")
    private LocalDateTime joinTime;
}