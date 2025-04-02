
package com.mvtalker.user.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_global_volume")
public class UserGlobalVolumePO
{
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;

    @TableField("output_volume")
    private Integer outputVolume;

    @TableField("output_active")
    private Boolean outputActive;

    @TableField("input_volume")
    private Integer inputVolume;

    @TableField("input_active")
    private Boolean inputActive;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}