
package com.mvtalker.user.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_local_volume")
public class UserLocalVolumePO
{
    @TableId(value = "agent_id", type = IdType.ASSIGN_ID)
    private Long agentId;

    @TableField("source_id")
    private Long sourceId;

    @TableField("target_id")
    private Long targetId;

    @TableField("input_volume")
    private Integer inputVolume;

    @TableField("input_active")
    private Boolean inputActive;
}