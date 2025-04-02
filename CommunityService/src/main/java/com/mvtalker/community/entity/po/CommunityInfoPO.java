
package com.mvtalker.community.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mvtalker.utilities.entity.community.enums.CommunityVisibility;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("community_info")
public class CommunityInfoPO
{
    @TableId(value = "community_id", type = IdType.ASSIGN_ID)
    private Long communityId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("max_members")
    private Integer maxMembers;

    @TableField("visibility")
    private CommunityVisibility visibility;

    @TableField("join_validation")
    private Boolean joinValidation;

    @TableField("community_code")
    private String communityCode;

    @TableField("icon_url")
    private String iconUrl;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}