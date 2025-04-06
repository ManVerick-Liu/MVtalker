
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
    public CommunityMemberDTO()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}