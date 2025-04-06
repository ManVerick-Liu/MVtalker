
package com.mvtalker.utilities.entity.user.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
public class UserLocalVolumeDTO
{
    //private Long agentId;
    private Long sourceId;
    private Long targetId;
    private Integer inputVolume;
    private Boolean inputActive;

    public UserLocalVolumeDTO(Long sourceId, Long targetId, Integer inputVolume, Boolean inputActive)
    {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.inputVolume = inputVolume;
        this.inputActive = inputActive;
    }
    public UserLocalVolumeDTO()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}