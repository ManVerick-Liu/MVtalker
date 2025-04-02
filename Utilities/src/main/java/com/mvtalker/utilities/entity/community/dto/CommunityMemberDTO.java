
package com.mvtalker.utilities.entity.community.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mvtalker.utilities.entity.community.enums.MemberRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityMemberDTO
{
    //private Long agentId;
    private Long communityId;
    private Long userId;
    private MemberRole role;

    public CommunityMemberDTO(Long communityId, Long userId, MemberRole role)
    {
        this.communityId = communityId;
        this.userId = userId;
        this.role = role;
    }
}