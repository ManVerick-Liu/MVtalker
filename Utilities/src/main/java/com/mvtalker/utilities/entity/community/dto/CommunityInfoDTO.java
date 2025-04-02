
package com.mvtalker.utilities.entity.community.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mvtalker.utilities.entity.community.enums.CommunityVisibility;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityInfoDTO
{
    private Long communityId;
    private String name;
    private String description;
    private Long creatorId;
    private Integer maxMembers;
    private CommunityVisibility visibility;
    private Boolean joinValidation;
    private String communityCode;
    private String iconUrl;

    public CommunityInfoDTO(Long communityId, String name, String description, Long creatorId, Integer maxMembers, CommunityVisibility visibility, Boolean joinValidation, String communityCode, String iconUrl)
    {
        this.communityId = communityId;
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.maxMembers = maxMembers;
        this.visibility = visibility;
        this.joinValidation = joinValidation;
        this.communityCode = communityCode;
        this.iconUrl = iconUrl;
    }
}